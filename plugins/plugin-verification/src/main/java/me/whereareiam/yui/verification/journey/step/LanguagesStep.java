package me.whereareiam.yui.verification.journey.step;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.annotation.journey.JourneyStep;
import me.whereareiam.yui.api.fluctlight.FluctlightService;
import me.whereareiam.yui.api.journey.definition.group.JourneyStepDefinition;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.model.journey.JourneyInstruction;
import me.whereareiam.yui.api.model.journey.JourneyPolicyDecision;
import me.whereareiam.yui.api.model.journey.JourneySignal;
import me.whereareiam.yui.api.model.journey.JourneyStepContext;
import me.whereareiam.yui.api.util.style.StyleKit;
import me.whereareiam.yui.verification.journey.Choice;
import me.whereareiam.yui.verification.journey.VerificationJourneys;
import me.whereareiam.yui.verification.journey.VerificationState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The second step: the member adds the other languages they speak, one click each, and goes on. Skipped when the
 * server offers no language they do not speak yet.
 */
@Component
@RequiredArgsConstructor
@JourneyStep(journeyId = VerificationJourneys.ID, stepId = "languages", order = 20)
public class LanguagesStep implements JourneyStepDefinition<VerificationState> {
	private final FluctlightService fluctlights;
	private final LanguageButtons buttons;

	@Override
	public @NotNull JourneyPolicyDecision evaluatePolicy(@NotNull JourneyStepContext<VerificationState> context) {
		Fluctlight member = fluctlights.get(context.state().getMember()).orElse(null);
		if (member == null || choices(member).isEmpty()) return JourneyPolicyDecision.skip();

		return JourneyPolicyDecision.allow();
	}

	@Override
	public @NotNull JourneyInstruction onEnter(@NotNull JourneyStepContext<VerificationState> context) {
		Fluctlight member = fluctlights.get(context.state().getMember()).orElse(null);
		if (member == null) return JourneyInstruction.fail();

		show(context, member);
		return JourneyInstruction.waitForSignal();
	}

	@Override
	public @NotNull JourneyInstruction onSignal(@NotNull JourneyStepContext<VerificationState> context, @NotNull JourneySignal signal) {
		Choice choice = Choice.of(signal).orElse(null);
		if (choice == Choice.CONTINUE) return JourneyInstruction.next();
		if (choice != Choice.LANGUAGE) return JourneyInstruction.waitForSignal();

		String locale = signal.attribute(Choice.LOCALE).orElse(null);
		Fluctlight member = fluctlights.get(context.state().getMember()).orElse(null);
		if (locale == null || member == null) return JourneyInstruction.waitForSignal();

		show(context, fluctlights.addAdditionalLanguage(member, DiscordLocale.from(locale)));
		return JourneyInstruction.waitForSignal();
	}

	private void show(JourneyStepContext<VerificationState> context, Fluctlight member) {
		VerificationState state = context.state();
		var rows = buttons.rows(choices(member), member, true, context.getSession().getId());
		Message message = state.getMessage();
		if (message == null) {
			state.getConversation().getChannel().sendMessageEmbeds(embed(member)).setComponents(rows).queue(state::setMessage);
			return;
		}

		message.editMessageEmbeds(embed(member)).setComponents(rows).queue(state::setMessage, error -> {
		});
	}

	private List<DiscordLocale> choices(Fluctlight member) {
		return buttons.offered(language -> !member.speaks(language));
	}

	private static MessageEmbed embed(Fluctlight member) {
		return StyleKit.embeds().primary()
				.setTitle(Translatable.text("verification.journey.languages.title").resolve(member))
				.setDescription(Translatable.text("verification.journey.languages.description").with("mention", member.getAsMention()).resolve(member))
				.build();
	}
}
