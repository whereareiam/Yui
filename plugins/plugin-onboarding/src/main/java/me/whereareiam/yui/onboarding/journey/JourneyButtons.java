package me.whereareiam.yui.onboarding.journey;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.annotation.ComponentListener;
import me.whereareiam.yui.api.journey.JourneyKeys;
import me.whereareiam.yui.api.journey.JourneyService;
import me.whereareiam.yui.api.model.journey.JourneySignal;
import me.whereareiam.yui.api.model.journey.session.JourneySession;
import me.whereareiam.yui.api.service.InteractionService;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;

/**
 * Turns a click on a step's message into a signal for the member's journey. Only the member onboarding is heard;
 * staff in the conversation and stale buttons change nothing.
 */
@Component
@RequiredArgsConstructor
public class JourneyButtons {
	private final InteractionService interactions;
	private final JourneyService journeys;

	@ComponentListener("language")
	public void onLanguage(ButtonInteractionEvent event) {
		String locale = interactions.getPayload(event);
		if (locale == null) {
			event.deferEdit().queue();
			return;
		}

		signal(event, Choice.LANGUAGE.signal().attribute(Choice.LOCALE, locale));
	}

	@ComponentListener("continue")
	public void onContinue(ButtonInteractionEvent event) {
		signal(event, Choice.CONTINUE.signal());
	}

	private void signal(ButtonInteractionEvent event, JourneySignal.Builder signal) {
		event.deferEdit().queue();

		long user = event.getUser().getIdLong();
		String session = interactions.getAttributes(event).get(JourneyKeys.SESSION_ID).orElse(null);
		JourneySession<OnboardingState> found = journeys.findSession(session, OnboardingState.class).orElse(null);
		if (found == null || found.getParticipantId() != user) return;

		journeys.signal(found.getId(), OnboardingState.class, signal.actorId(user).build());
	}
}
