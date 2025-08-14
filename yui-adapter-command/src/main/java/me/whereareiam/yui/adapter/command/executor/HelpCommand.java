package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.adapter.command.registry.CommandRegistry;
import me.whereareiam.yui.adapter.command.registry.CommandDefinition;
import me.whereareiam.yui.api.annotation.Command;
import me.whereareiam.yui.api.annotation.ComponentListener;
import me.whereareiam.yui.api.output.CommandBase;
import me.whereareiam.yui.api.style.StyleKit;
import me.whereareiam.yui.api.type.CommandCategory;
import me.whereareiam.yui.api.util.Components;
import me.whereareiam.yui.api.util.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class HelpCommand implements CommandBase {
	private final CommandRegistry commandRegistry;

	@Command(name = "help")
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping optionMapping = event.getOption("category");

		if (optionMapping != null) {
			CommandCategory category = CommandCategory.valueOf(optionMapping.getAsString().toUpperCase());
			categoryHelp(event, category);

			return;
		}

		globalHelp(event);
	}

	@ComponentListener("help_category")
	private void onSelectMenu(StringSelectInteractionEvent event) {
		CommandCategory category = CommandCategory.valueOf(event.getValues().getFirst().toUpperCase());

		categoryHelp(event, category);
	}

	private void globalHelp(SlashCommandInteractionEvent event) {
		EmbedBuilder embed = StyleKit.embeds().primary();
		embed.setTitle(Translatable.of("commands.help.information.global.title", event.getUser().getIdLong()));
		embed.setDescription(Translatable.of("commands.help.information.global.description", event.getUser().getIdLong()));

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

		StringSelectMenu selectMenu = Components.menu("help_category")
				.addOptions(options)
				.build();

		event.replyEmbeds(embed.build())
				.setEphemeral(true)
				.addActionRow(selectMenu)
				.queue();
	}

	private void categoryHelp(IReplyCallback event, CommandCategory category) {
		long userId = event.getUser().getIdLong();

		EmbedBuilder embed = StyleKit.embeds().primary();
		embed.setTitle(Translatable.forUser(
				"commands.help.information.specific.title",
				userId,
				Translatable.of(category.getKey(), userId)
		));
		embed.setDescription(Translatable.forUser(
				"commands.help.information.specific.description",
				userId,
				Translatable.of(category.getKey(), userId)
		));

		// Get commands for this category, showing only primary command names (not aliases)
		commandRegistry.getDefinitions().entrySet().stream()
				.filter(entry -> entry.getValue().getCommandConfig().getCategory() == category)
				.filter(entry -> {
					// Only show the entry if it's the primary command name, not an alias
					String key = entry.getKey();
					CommandDefinition def = entry.getValue();
					String primaryName = def.getCommandName().toLowerCase();
					return key.equals(primaryName);
				})
				.forEach(entry -> {
					CommandDefinition def = entry.getValue();
					String name = def.getCommandName();
					String example = def.getCommandConfig().getExample();
					String description = def.getCommandConfig().getDescription();

					embed.addField(
							Translatable.forUser(
									"commands.help.information.specific.headFormat",
									userId,
									Translatable.of(name, userId)
							),
							Translatable.forUser(
									"commands.help.information.specific.footFormat",
									userId,
									Translatable.of(example, userId),
									Translatable.of(description, userId)
							),
							false
					);
				});

		if (event instanceof StringSelectInteractionEvent selectEvent) {
			selectEvent.editMessageEmbeds(embed.build()).queue();
			return;
		}

		event.replyEmbeds(embed.build())
				.setEphemeral(true)
				.queue();
	}
}
