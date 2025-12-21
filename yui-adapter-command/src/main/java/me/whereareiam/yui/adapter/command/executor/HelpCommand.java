package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.annotation.command.Optional;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.command.CommandService;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.translation.Translatable;
import me.whereareiam.yui.type.CommandCategory;
import me.whereareiam.yui.util.Components;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class HelpCommand {
	private final CommandService commandService;

	private static final String CATEGORY_LISTENER = "command_help_category";

	@Definition("help")
	@Command("help [category]")
	public void onCommand(
			Interaction interaction,
			@Optional @Argument("category") String categoryName
	) {
		if (categoryName != null && !categoryName.isBlank()) {
			CommandCategory category = CommandCategory.valueOf(categoryName.toUpperCase());
			categoryHelp(interaction.replyCallback(), interaction.fluctlight(), category);
			return;
		}

		globalHelp(interaction.replyCallback(), interaction.fluctlight());
	}

	@ComponentListener(CATEGORY_LISTENER)
	private void onSelectMenu(Fluctlight fluctlight, StringSelectInteractionEvent event) {
		CommandCategory category = CommandCategory.valueOf(event.getValues().getFirst().toUpperCase());
		categoryHelp(event, fluctlight, category);
	}

	private void globalHelp(IReplyCallback reply, Fluctlight fluctlight) {
		EmbedBuilder embed = StyleKit.embeds().primary();
		embed.setTitle(Translatable.text("commands.help.information.global.title").resolve(fluctlight));
		embed.setDescription(Translatable.text("commands.help.information.global.description").resolve(fluctlight));

		for (CommandCategory category : CommandCategory.values()) {
			if (category == CommandCategory.NONE)
				continue;

			embed.addField(
					Translatable.text(category.getKey()).resolve(fluctlight),
					Translatable.text("commands.help.category." + category.name().toLowerCase()).resolve(fluctlight),
					false
			);
		}

		List<SelectOption> options = Arrays.stream(CommandCategory.values())
				.filter(category -> category != CommandCategory.NONE)
				.map(category -> SelectOption.of(
						Translatable.text(category.getKey()).resolve(fluctlight),
						category.name().toLowerCase()
				))
				.toList();

		StringSelectMenu selectMenu = Components.menu(CATEGORY_LISTENER)
				.addOptions(options)
				.build();

		reply.replyEmbeds(embed.build())
				.setEphemeral(true)
				.addActionRow(selectMenu)
				.queue();
	}

	private void categoryHelp(IReplyCallback event, Fluctlight fluctlight, CommandCategory category) {
		EmbedBuilder embed = StyleKit.embeds().primary();
		
		String categoryName = Translatable.text(category.getKey()).resolve(fluctlight);
		
		embed.setTitle(Translatable.text("commands.help.information.specific.title")
			.with("categoryName", categoryName)
			.resolve(fluctlight));
		
		embed.setDescription(Translatable.text("commands.help.information.specific.description")
			.with("categoryName", categoryName)
			.resolve(fluctlight));

		// Get commands for this category, showing only primary command names (first alias)
		for (Map.Entry<String, CommandDefinition> entry : commandService.getDefinitions().entrySet().stream()
				.filter(e -> e.getValue().getCategory() == category)
				.sorted(Comparator.comparing(e -> {
					List<String> aliases = e.getValue().getAliases();
					return (aliases == null || aliases.isEmpty()) ? "" : aliases.getFirst();
				}))
				.toList()) {

			CommandDefinition def = entry.getValue();
			List<String> aliases = def.getAliases();
			if (aliases == null || aliases.isEmpty())
				continue;

			String primaryName = aliases.getFirst();
			String example = def.getExample();
			String description = def.getDescription();

			String commandName = Translatable.text(primaryName).resolve(fluctlight);
			String exampleText = Translatable.text(example).resolve(fluctlight);
			String descriptionText = Translatable.text(description).resolve(fluctlight);

			embed.addField(
				Translatable.text("commands.help.information.specific.headFormat")
					.with("commandName", commandName)
					.resolve(fluctlight),
				Translatable.text("commands.help.information.specific.footFormat")
					.with("example", exampleText)
					.with("description", descriptionText)
					.resolve(fluctlight),
				false
			);
		}

		if (event instanceof StringSelectInteractionEvent selectEvent) {
			selectEvent.editMessageEmbeds(embed.build()).queue();
			return;
		}

		event.replyEmbeds(embed.build())
				.setEphemeral(true)
				.queue();
	}
}
