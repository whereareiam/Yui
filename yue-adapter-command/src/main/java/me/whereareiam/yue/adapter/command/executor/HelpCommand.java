package me.whereareiam.yue.adapter.command.executor;

import me.whereareiam.yue.api.component.Translatable;
import me.whereareiam.yue.api.output.command.Command;
import me.whereareiam.yue.api.output.command.CommandBase;
import me.whereareiam.yue.api.type.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class HelpCommand implements CommandBase {
	@Command(name = "help")
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping optionMapping = event.getOption("category");

		if (optionMapping != null) {
			categoryHelp(event);
			return;
		}

		globalHelp(event);
	}

	private void globalHelp(SlashCommandInteractionEvent event) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(Translatable.of("commands.help.information.global.title", event.getUser().getIdLong()));
		embed.setDescription(Translatable.of("commands.help.information.global.description", event.getUser().getIdLong()));
		embed.setColor(Color.PINK);

		for (CommandCategory category : CommandCategory.values()) {
			if (category == CommandCategory.NONE)
				continue;

			embed.addField(
					Translatable.of(category.getKey(), event.getUser().getIdLong()),
					Translatable.of("commands.help.category." + category.name().toLowerCase(), event.getUser().getIdLong()),
					false
			);
		}

		event.replyEmbeds(embed.build())
				.setEphemeral(true)
				.queue();
	}

	private void categoryHelp(SlashCommandInteractionEvent event) {
		event.reply("Category help").queue();
	}
}
