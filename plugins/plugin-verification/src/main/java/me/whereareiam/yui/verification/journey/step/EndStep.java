package me.whereareiam.yui.verification.journey.step;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.annotation.journey.JourneyStep;
import me.whereareiam.yui.api.journey.definition.group.JourneyStepDefinition;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.journey.JourneyInstruction;
import me.whereareiam.yui.api.model.journey.JourneyStepContext;
import me.whereareiam.yui.api.util.style.StyleKit;
import me.whereareiam.yui.verification.journey.VerificationJourneys;
import me.whereareiam.yui.verification.journey.VerificationState;
import me.whereareiam.yui.verification.service.VerifiedMembers;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * The last step: the member is verified and reads the end text, which may point to the rules.
 */
@Component
@RequiredArgsConstructor
@JourneyStep(journeyId = VerificationJourneys.ID, stepId = "end", order = 999)
public class EndStep implements JourneyStepDefinition<VerificationState> {
	private final VerifiedMembers verified;

	@Override
	public @NotNull JourneyInstruction onEnter(@NotNull JourneyStepContext<VerificationState> context) {
		VerificationState state = context.state();
		verified.verify(state.getMember());

		Message message = state.getMessage();
		if (message == null) state.getConversation().getChannel().sendMessageEmbeds(embed(state.getMember())).queue();
		else message.editMessageEmbeds(embed(state.getMember())).setComponents().queue(state::setMessage, error -> {
		});

		return JourneyInstruction.complete();
	}

	private static MessageEmbed embed(long member) {
		return StyleKit.embeds().success()
				.setTitle(Translatable.text("verification.journey.end.title").resolve(member))
				.setDescription(Translatable.text("verification.journey.end.description").with("mention", "<@" + member + ">").resolve(member))
				.build();
	}
}
