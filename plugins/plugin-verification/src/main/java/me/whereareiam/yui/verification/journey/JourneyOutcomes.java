package me.whereareiam.yui.verification.journey;

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
import me.whereareiam.yui.verification.model.VerificationSettings;
import me.whereareiam.yui.verification.service.VerificationAudit;
import me.whereareiam.yui.verification.type.DeadlineAction;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * What follows the end of a verification: its conversation closes, the audit trail hears how it ended, and a member
 * past the deadline meets the deadline's action. A member who left or closed the conversation is posted as abandoned
 * where that happened.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JourneyOutcomes {
	private final ConversationService conversations;
	private final VerificationAudit audit;
	private final ObjectProvider<VerificationSettings> settings;
	private final ObjectProvider<Settings> yui;
	private final JDA jda;

	@EventListener
	public void onCompleted(JourneyCompletedEvent event) {
		VerificationState state = state(event);
		if (state == null) return;

		conversations.close(state.getConversation());
		audit.post(VerificationAudit.Entry.COMPLETED, state.getMember());
	}

	@EventListener
	public void onCancelled(JourneyCancelledEvent event) {
		VerificationState state = state(event);
		if (state != null) conversations.close(state.getConversation());
	}

	@EventListener
	public void onFailed(JourneyFailedEvent event) {
		VerificationState state = state(event);
		if (state == null) return;

		conversations.close(state.getConversation());
		audit.post(VerificationAudit.Entry.FAILED, state.getMember());
	}

	@EventListener
	public void onTimedOut(JourneySessionTimeoutEvent event) {
		VerificationState state = state(event);
		if (state == null) return;

		audit.post(VerificationAudit.Entry.TIMEOUT, state.getMember());
		if (settings.getObject().getDeadline().getAction() == DeadlineAction.KICK) kick(state.getMember());
	}

	private void kick(long member) {
		Guild guild = jda.getGuildById(yui.getObject().getGuild());
		if (guild == null) return;

		String reason = Translatable.text("verification.settings.deadline.kick").resolve(member);
		guild.kick(UserSnowflake.fromId(member)).reason(reason).queue(
				done -> log.info("[Verification] Kicked {}: {}", member, reason),
				error -> log.warn("[Verification] Could not kick {}: {}", member, error.getMessage())
		);
	}

	private static VerificationState state(JourneyEvent event) {
		if (!VerificationJourneys.ID.equals(event.getSession().getJourneyId())) return null;

		return event.getSession().getState(VerificationState.class);
	}
}
