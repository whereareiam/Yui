package me.whereareiam.yui.onboarding.journey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.conversation.ConversationService;
import me.whereareiam.yui.api.event.journey.JourneyCancelledEvent;
import me.whereareiam.yui.api.event.journey.JourneyCompletedEvent;
import me.whereareiam.yui.api.event.journey.JourneyFailedEvent;
import me.whereareiam.yui.api.event.journey.base.JourneyEvent;
import me.whereareiam.yui.api.event.journey.session.JourneySessionTimeoutEvent;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.onboarding.model.OnboardingSettings;
import me.whereareiam.yui.onboarding.service.OnboardingAudit;
import me.whereareiam.yui.onboarding.type.DeadlineAction;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * What follows the end of onboarding: its conversation closes, the audit trail hears how it ended, and a member
 * past the deadline meets the deadline's action. A member who left or closed the conversation is posted as abandoned
 * where that happened.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JourneyOutcomes {
	private final ConversationService conversations;
	private final OnboardingAudit audit;
	private final ObjectProvider<OnboardingSettings> settings;
	private final ObjectProvider<Settings> yui;
	private final JDA jda;

	@EventListener
	public void onCompleted(JourneyCompletedEvent event) {
		OnboardingState state = state(event);
		if (state == null) return;

		conversations.close(state.getConversation());
		audit.post(OnboardingAudit.Entry.COMPLETED, state.getMember());
	}

	@EventListener
	public void onCancelled(JourneyCancelledEvent event) {
		OnboardingState state = state(event);
		if (state != null) conversations.close(state.getConversation());
	}

	@EventListener
	public void onFailed(JourneyFailedEvent event) {
		OnboardingState state = state(event);
		if (state == null) return;

		conversations.close(state.getConversation());
		audit.post(OnboardingAudit.Entry.FAILED, state.getMember());
	}

	@EventListener
	public void onTimedOut(JourneySessionTimeoutEvent event) {
		OnboardingState state = state(event);
		if (state == null) return;

		audit.post(OnboardingAudit.Entry.TIMEOUT, state.getMember());
		if (settings.getObject().getDeadline().getAction() == DeadlineAction.KICK) kick(state.getMember());
	}

	private void kick(long member) {
		Guild guild = jda.getGuildById(yui.getObject().getGuild());
		if (guild == null) return;

		String reason = Translatable.text("onboarding.settings.deadline.kick").resolve(member);
		guild.kick(UserSnowflake.fromId(member)).reason(reason).queue(
				done -> log.info("[Onboarding] Kicked {}: {}", member, reason),
				error -> log.warn("[Onboarding] Could not kick {}: {}", member, error.getMessage())
		);
	}

	private static OnboardingState state(JourneyEvent event) {
		if (!OnboardingJourneys.ID.equals(event.getSession().getJourneyId())) return null;

		return event.getSession().getState(OnboardingState.class);
	}
}
