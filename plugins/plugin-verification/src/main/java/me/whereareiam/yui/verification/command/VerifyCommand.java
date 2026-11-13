package me.whereareiam.yui.verification.command;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.annotation.command.Argument;
import me.whereareiam.yui.api.annotation.command.Command;
import me.whereareiam.yui.api.annotation.command.Definition;
import me.whereareiam.yui.api.command.Interaction;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.util.style.StyleKit;
import me.whereareiam.yui.verification.api.VerificationService.Start;
import me.whereareiam.yui.verification.api.VerificationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * {@code /verify <user>}: a moderator starts a verification for a member, and reads what happened.
 */
@Component
@RequiredArgsConstructor
public class VerifyCommand {
	private static final Map<Start, Supplier<EmbedBuilder>> KINDS = new EnumMap<>(Map.of(
			Start.STARTED, () -> StyleKit.embeds().success(),
			Start.VERIFIED, () -> StyleKit.embeds().info(),
			Start.RUNNING, () -> StyleKit.embeds().info(),
			Start.REFUSED, () -> StyleKit.embeds().error()
	));

	private final VerificationService verification;

	@Definition("verify")
	@Command("verify <user>")
	public void onCommand(Interaction interaction, @Argument("user") Fluctlight target) {
		Fluctlight moderator = interaction.fluctlight();
		interaction.deferReply(true).queue();

		verification.start(target.getId(), moderator.getId())
				.thenAccept(start -> interaction.getHook().editOriginalEmbeds(reply(start, target, moderator)).queue());
	}

	private static MessageEmbed reply(Start start, Fluctlight target, Fluctlight moderator) {
		String texts = "verification.commands.verify." + start.name().toLowerCase(Locale.ROOT) + ".";

		return KINDS.get(start).get()
				.setTitle(Translatable.text(texts + "title").resolve(moderator))
				.setDescription(Translatable.text(texts + "description").with("user", target.getAsMention()).resolve(moderator))
				.build();
	}
}
