package me.whereareiam.yui.interests;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.annotation.ComponentListener;
import me.whereareiam.yui.api.annotation.journey.JourneyStep;
import me.whereareiam.yui.api.fluctlight.FluctlightService;
import me.whereareiam.yui.api.journey.JourneyKeys;
import me.whereareiam.yui.api.journey.JourneyService;
import me.whereareiam.yui.api.journey.definition.group.JourneyStepDefinition;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.component.ComponentAttributes;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.model.journey.JourneyInstruction;
import me.whereareiam.yui.api.model.journey.JourneyPolicyDecision;
import me.whereareiam.yui.api.model.journey.JourneySignal;
import me.whereareiam.yui.api.model.journey.JourneyStepContext;
import me.whereareiam.yui.api.model.journey.session.JourneySession;
import me.whereareiam.yui.api.service.InteractionService;
import me.whereareiam.yui.api.util.style.StyleKit;
import me.whereareiam.yui.interests.model.Topics;
import me.whereareiam.yui.interests.service.TopicMenu;
import me.whereareiam.yui.interests.service.TopicPicks;
import me.whereareiam.yui.onboarding.api.OnboardingService;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A step of onboarding, after the languages: the member picks the topics they are here for on the onboarding message,
 * then goes on. Skipped while {@code topics.yml} offers none.
 */
@Component
@RequiredArgsConstructor
@JourneyStep(journeyId = TopicsStep.JOURNEY, stepId = "topics", order = 30)
public class TopicsStep implements JourneyStepDefinition<Object> {
	/**
	 * Onboarding's journey, which the step joins.
	 */
	static final String JOURNEY = "onboarding";

	private static final String DONE = "topics-done";

	private final OnboardingService onboarding;
	private final JourneyService journeys;
	private final FluctlightService fluctlights;
	private final InteractionService interactions;
	private final ObjectProvider<Topics> topics;
	private final TopicPicks picks;
	private final TopicMenu menu;

	@Override
	public @NotNull JourneyPolicyDecision evaluatePolicy(@NotNull JourneyStepContext<Object> context) {
		return topics.getObject().all().isEmpty() ? JourneyPolicyDecision.skip() : JourneyPolicyDecision.allow();
	}

	@Override
	public @NotNull JourneyInstruction onEnter(@NotNull JourneyStepContext<Object> context) {
		long id = context.getSession().getParticipantId();
		Fluctlight member = fluctlights.get(id).orElse(null);
		if (member == null) return JourneyInstruction.fail();

		ComponentAttributes attributes = JourneyKeys.forSession(context.getSession().getId());
		String label = Translatable.text("words.proceed").resolve(member);
		Button done = interactions.createButton(ButtonStyle.SUCCESS, DONE, label, attributes);
		onboarding.show(id, embed(member), List.of(
				ActionRow.of(menu.menu(member, picks.of(id), attributes)),
				ActionRow.of(done)
		));

		return JourneyInstruction.waitForSignal();
	}

	@Override
	public @NotNull JourneyInstruction onSignal(@NotNull JourneyStepContext<Object> context, @NotNull JourneySignal signal) {
		return DONE.equals(signal.getType()) ? JourneyInstruction.next() : JourneyInstruction.waitForSignal();
	}

	@ComponentListener(DONE)
	public void onDone(ButtonInteractionEvent event) {
		event.deferEdit().queue();

		long user = event.getUser().getIdLong();
		String session = interactions.getAttributes(event).get(JourneyKeys.SESSION_ID).orElse(null);
		JourneySession<Object> found = journeys.findSession(session, Object.class).orElse(null);
		if (found == null || found.getParticipantId() != user) return;

		journeys.signal(found.getId(), Object.class, JourneySignal.builder(DONE).actorId(user).build());
	}

	private static MessageEmbed embed(Fluctlight member) {
		return StyleKit.embeds().primary()
				.setTitle(Translatable.text("interests.journey.topics.title").resolve(member))
				.setDescription(Translatable.text("interests.journey.topics.description").resolve(member))
				.build();
	}
}
