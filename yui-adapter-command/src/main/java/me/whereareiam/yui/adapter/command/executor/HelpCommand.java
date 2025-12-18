package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.annotation.command.Optional;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.service.CommandService;
import me.whereareiam.yui.style.StyleKit;
import me.whereareiam.yui.translation.Translatable;
import me.whereareiam.yui.type.CommandCategory;
import me.whereareiam.yui.util.Components;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.incendo.cloud.discord.jda6.JDAInteraction;
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
			JDAInteraction interaction,
			@Optional @Argument("category") String categoryName
	) {
		long userId = interaction.user().getIdLong();

		if (categoryName != null && !categoryName.isBlank()) {
			CommandCategory category = CommandCategory.valueOf(categoryName.toUpperCase());
			categoryHelp(interaction.replyCallback(), category);
			return;
		}

		globalHelp(interaction.replyCallback(), userId);
	}

	@ComponentListener(CATEGORY_LISTENER)
	private void onSelectMenu(StringSelectInteractionEvent event) {
		CommandCategory category = CommandCategory.valueOf(event.getValues().getFirst().toUpperCase());

		categoryHelp(event, category);
	}

	private void globalHelp(IReplyCallback reply, long userId) {
		EmbedBuilder embed = StyleKit.embeds().primary();
		embed.setTitle(Translatable.of("commands.help.information.global.title", userId));
		embed.setDescription(Translatable.of("commands.help.information.global.description", userId));

		for (CommandCategory category : CommandCategory.values()) {
			if (category == CommandCategory.NONE)
				continue;

			embed.addField(
					Translatable.of(category.getKey(), userId),
					Translatable.of("commands.help.category." + category.name().toLowerCase(), userId),
					false
			);
		}

		List<SelectOption> options = Arrays.stream(CommandCategory.values())
				.filter(category -> category != CommandCategory.NONE)
				.map(category -> SelectOption.of(
						Translatable.of(category.getKey(), userId),
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

			embed.addField(
					Translatable.forUser(
							"commands.help.information.specific.headFormat",
							userId,
							Translatable.of(primaryName, userId)
					),
					Translatable.forUser(
							"commands.help.information.specific.footFormat",
							userId,
							Translatable.of(example, userId),
							Translatable.of(description, userId)
					),
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
