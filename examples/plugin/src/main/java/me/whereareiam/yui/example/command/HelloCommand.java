package me.whereareiam.yui.example.command;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.annotation.command.Command;
import me.whereareiam.yui.api.annotation.command.Definition;
import me.whereareiam.yui.api.command.Interaction;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.example.model.GreetingSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code /hello}: greets whoever runs it, in their language.
 */
@Component
@RequiredArgsConstructor
public class HelloCommand {
	private final ObjectProvider<GreetingSettings> settings;

	@Definition("hello")
	@Command("hello")
	public void onCommand(Interaction interaction) {
		Fluctlight member = interaction.fluctlight();
		String reply = Translatable.text("greeting.commands.hello.reply")
				.with("mention", member.getAsMention())
				.with("emoji", settings.getObject().getEmoji())
				.resolve(member);

		interaction.replyCallback().reply(reply).setEphemeral(true).queue();
	}
}
