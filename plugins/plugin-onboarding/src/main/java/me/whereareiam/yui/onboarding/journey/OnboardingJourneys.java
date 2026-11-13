package me.whereareiam.yui.onboarding.journey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.annotation.journey.JourneyConfiguration;
import me.whereareiam.yui.api.conversation.Conversation;
import me.whereareiam.yui.api.conversation.ConversationOptions;
import me.whereareiam.yui.api.conversation.ConversationService;
import me.whereareiam.yui.api.event.conversation.ConversationClosedEvent;
import me.whereareiam.yui.api.fluctlight.FluctlightService;
import me.whereareiam.yui.api.journey.JourneyKeys;
import me.whereareiam.yui.api.journey.JourneyService;
import me.whereareiam.yui.api.journey.definition.JourneyConfigurationDefinition;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.config.conversation.Conversations;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.model.journey.session.JourneySession;
import me.whereareiam.yui.api.model.journey.session.JourneySessionRequest;
import me.whereareiam.yui.api.type.journey.JourneyStatus;
import me.whereareiam.yui.onboarding.model.OnboardingSettings;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * The journey {@code onboarding}: welcome (the primary language), languages (the others) and end, in a conversation
 * of the purpose {@code onboarding}. The conversation stays open while the member verifies and closes when the
 * journey ends; when it closes before, deleted by hand, the journey ends with it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@JourneyConfiguration(journeyId = OnboardingJourneys.ID)
public class OnboardingJourneys implements JourneyConfigurationDefinition {
	/**
	 * The journey's id and the conversation's purpose.
	 */
	public static final String ID = "onboarding";

	private static final Set<JourneyStatus> ACTIVE = EnumSet.of(JourneyStatus.RUNNING, JourneyStatus.WAITING);

	private final JourneyService journeys;
	private final ConversationService conversations;
	private final FluctlightService fluctlights;
	private final ObjectProvider<OnboardingSettings> settings;

	public boolean isRunning(long member) {
		return session(member).isPresent();
	}

	/**
	 * Opens the conversation and starts the journey in it.
	 *
	 * @return whether it started; false when Yui does not know the member or no conversation opened
	 */
	public CompletableFuture<Boolean> start(long member, @Nullable Long initiator) {
		Fluctlight fluctlight = fluctlights.get(member).orElse(null);
		if (fluctlight == null) return CompletableFuture.completedFuture(false);

		return conversations.open(ID, List.of(member), options(fluctlight))
				.thenApply(conversation -> {
					begin(member, conversation, initiator);
					return true;
				})
				.exceptionally(error -> {
					log.warn("[Onboarding] No conversation for {}: {}", member, error.getMessage());
					return false;
				});
	}

	/**
	 * Ends the member's onboarding, if one runs.
	 *
	 * @return whether one ran
	 */
	public boolean cancel(long member) {
		return session(member).map(session -> journeys.cancel(session.getId())).orElse(false);
	}

	public void cancelAll() {
		journeys.findAll(ID, ACTIVE).forEach(session -> journeys.cancel(session.getId()));
	}

	@EventListener
	public void onConversationClosed(ConversationClosedEvent event) {
		Conversation conversation = event.getConversation();
		if (!ID.equals(conversation.getPurpose())) return;

		conversation.getMembers().forEach(this::cancel);
	}

	private void begin(long member, Conversation conversation, @Nullable Long initiator) {
		OnboardingState state = new OnboardingState(member, conversation, initiator);
		JourneySessionRequest.Builder<OnboardingState> request = JourneySessionRequest.builder(ID, member, OnboardingState.class, state);
		OnboardingSettings.Deadline deadline = settings.getObject().getDeadline();
		if (deadline.isEnabled()) request.attribute(JourneyKeys.TIMEOUT_SECONDS, deadline.getAfter().getSeconds());

		journeys.start(request.build());
	}

	private ConversationOptions options(Fluctlight fluctlight) {
		return ConversationOptions.builder()
				.name(Translatable.text("onboarding.journey.conversation.name").with("name", fluctlight.getName()).resolve(fluctlight))
				.message(Translatable.text("onboarding.journey.conversation.message").with("mention", fluctlight.getAsMention()).resolve(fluctlight))
				.close(close())
				.build();
	}

	// Open while the member verifies, however long: the journey closes it when it ends, the deadline included.
	private static Conversations.Close close() {
		return new Conversations.Close();
	}

	private Optional<JourneySession<OnboardingState>> session(long member) {
		return journeys.find(ID, member, ACTIVE, OnboardingState.class);
	}
}
