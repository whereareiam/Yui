package me.whereareiam.yui.verification.journey.step;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.annotation.journey.JourneyStep;
import me.whereareiam.yui.api.fluctlight.FluctlightService;
import me.whereareiam.yui.api.journey.definition.group.JourneyStepDefinition;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.model.journey.JourneyInstruction;
import me.whereareiam.yui.api.model.journey.JourneySignal;
import me.whereareiam.yui.api.model.journey.JourneyStepContext;
import me.whereareiam.yui.api.util.style.StyleKit;
import me.whereareiam.yui.verification.journey.Choice;
import me.whereareiam.yui.verification.journey.VerificationJourneys;
import me.whereareiam.yui.verification.journey.VerificationState;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * The first step: the member picks their primary language. The continue button appears once they picked one.
 */
@Component
@RequiredArgsConstructor
@JourneyStep(journeyId = VerificationJourneys.ID, stepId = "welcome", order = 10)
public class WelcomeStep implements JourneyStepDefinition<VerificationState> {
	private final FluctlightService fluctlights;
	private final LanguageButtons buttons;

	@Override
	public @NotNull JourneyInstruction onEnter(@NotNull JourneyStepContext<VerificationState> context) {
		VerificationState state = context.state();
		Fluctlight member = fluctlights.get(state.getMember()).orElse(null);
		if (member == null) return JourneyInstruction.fail();

		state.getConversation().getChannel()
				.sendMessageEmbeds(embed(member))
				.setComponents(buttons.rows(buttons.offered(language -> true), member, false, context.getSession().getId()))
				.queue(state::setMessage);

		return JourneyInstruction.waitForSignal();
	}

	@Override
	public @NotNull JourneyInstruction onSignal(@NotNull JourneyStepContext<VerificationState> context, @NotNull JourneySignal signal) {
		Choice choice = Choice.of(signal).orElse(null);
		if (choice == Choice.CONTINUE) return JourneyInstruction.next();
		if (choice != Choice.LANGUAGE) return JourneyInstruction.waitForSignal();

		VerificationState state = context.state();
		String locale = signal.attribute(Choice.LOCALE).orElse(null);
		Fluctlight member = fluctlights.get(state.getMember()).orElse(null);
		if (locale == null || member == null || state.getMessage() == null) return JourneyInstruction.waitForSignal();

		Fluctlight changed = fluctlights.changePrimaryLanguage(member, DiscordLocale.from(locale));
		DiscordLocale primary = changed.getPrimaryLanguage();
		state.getMessage().editMessageEmbeds(embed(changed))
				.setComponents(buttons.rows(buttons.offered(language -> language != primary), changed, true, context.getSession().getId()))
				.queue(state::setMessage, error -> {
				});

		return JourneyInstruction.waitForSignal();
	}

	private static MessageEmbed embed(Fluctlight member) {
		return StyleKit.embeds().primary()
				.setTitle(Translatable.text("verification.journey.welcome.title").resolve(member))
				.setDescription(Translatable.text("verification.journey.welcome.description").with("mention", member.getAsMention()).resolve(member))
				.build();
	}
}
