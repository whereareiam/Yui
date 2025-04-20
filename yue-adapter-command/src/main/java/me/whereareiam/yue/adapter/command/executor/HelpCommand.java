package me.whereareiam.yue.adapter.command.executor;

import me.whereareiam.yue.api.Components;
import me.whereareiam.yue.api.Translatable;
import me.whereareiam.yue.api.annotation.Command;
import me.whereareiam.yue.api.annotation.ComponentListener;
import me.whereareiam.yue.api.output.CommandBase;
import me.whereareiam.yue.api.type.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

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

		List<SelectOption> options = Arrays.stream(CommandCategory.values())
				.filter(category -> category != CommandCategory.NONE)
				.map(category -> SelectOption.of(
						Translatable.of(category.getKey(), event.getUser().getIdLong()),
						category.name().toLowerCase()
				))
				.toList();

		StringSelectMenu selectMenu = Components.menu("help_category", options);

		event.replyEmbeds(embed.build())
				.setEphemeral(true)
				.addActionRow(selectMenu)
				.queue();
	}

	private void categoryHelp(SlashCommandInteractionEvent event) {
		event.reply("Category help").queue();
	}

	@ComponentListener("help_category")
	private void onSelectMenu(StringSelectInteractionEvent event) {
		System.out.println(event.getComponentId());
	}
}
