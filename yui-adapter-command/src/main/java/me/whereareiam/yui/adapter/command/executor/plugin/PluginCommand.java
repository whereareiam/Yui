package me.whereareiam.yui.adapter.command.executor.plugin;

import lombok.AllArgsConstructor;
import me.whereareiam.chassis.exception.ExtensionException;
import me.whereareiam.chassis.model.InstalledExtension;
import me.whereareiam.chassis.type.ExtensionState;
import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.message.Translatable;
import me.whereareiam.yui.model.PayloadButton;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.plugin.PluginManager;
import me.whereareiam.yui.util.Components;
import me.whereareiam.yui.util.style.StyleKit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
public class PluginCommand {
	private final PluginManager pluginManager;
	private final FluctlightService fluctlightService;

	private static final String CATEGORY_LISTENER = "command_plugin_category";
	private static final String SELECT_LISTENER = "command_plugin_select";
	private static final String BACK_LISTENER = "command_plugin_back";
	private static final String RESCAN_ACTION = "rescan";

	private static final String RELOAD_ALL_LISTENER = "command_plugin_reload_all";
	private static final String RELOAD_ALL_CONFIRM_LISTENER = "command_plugin_reload_all_confirm";
	private static final String RELOAD_ALL_CANCEL_LISTENER = "command_plugin_reload_all_cancel";

	private static final int BUTTONS_PER_ROW = 5;

	@Definition("plugin")
	@Command("plugin [action] [plugin]")
	public void onCommand(
			Interaction interaction,
			@Argument("action") String action,
			@Argument("plugin") String pluginArg
	) {
		Fluctlight fluctlight = interaction.fluctlight();

		if (action == null) {
			interaction.replyCallback()
					.replyEmbeds(buildMainEmbed(fluctlight).build())
					.setEphemeral(true)
					.addActionRow(mainControls(fluctlight))
					.queue();
			return;
		}

		if (!Category.isSupported(action)) {
			interaction.replyCallback()
					.replyEmbeds(StyleKit.embeds().error()
									.setTitle(Translatable.text("commands.error.validation.invalidButton").resolve(fluctlight))
									.build())
					.setEphemeral(true)
					.queue();
			return;
		}

		if (pluginArg == null) {
			renderCategory(interaction.replyCallback(), fluctlight, action, false);
			return;
		}

		performActionDirect(interaction.replyCallback(), fluctlight, action, pluginArg);
	}

	@ComponentListener(CATEGORY_LISTENER)
	public void onCategory(Fluctlight fluctlight, ButtonInteractionEvent event) {
		String payload = Components.payload(event);
		
		if (payload == null) {
			event.deferEdit().queue();
			event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.text("commands.error.validation.invalidButton").resolve(fluctlight))
							.build())
					.setComponents()
					.queue();
			return;
		}

		event.deferEdit().queue();
		renderCategory(event, fluctlight, payload, true);
	}

	@ComponentListener(SELECT_LISTENER)
	public void onSelect(Fluctlight fluctlight, ButtonInteractionEvent event) {
		String payload = Components.payload(event);
		
		if (payload == null || !payload.contains("|")) {
			event.deferEdit().queue();
			event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.text("commands.error.validation.invalidButton").resolve(fluctlight))
							.build())
					.setComponents()
					.queue();
			return;
		}

		String action = payload.substring(0, payload.indexOf('|'));
		String value = payload.substring(payload.indexOf('|') + 1);

		event.deferEdit().queue();
		performActionAndReport(event, fluctlight, action, value);
	}

	@ComponentListener(BACK_LISTENER)
	public void onBack(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		long userId = event.getUser().getIdLong();
		Optional<Fluctlight> fluctlightOpt = fluctlightService.get(userId);
		if (fluctlightOpt.isEmpty()) {
			return;
		}
		Fluctlight fluctlight = fluctlightOpt.get();

		event.getHook()
				.editOriginalEmbeds(buildMainEmbed(fluctlight).build())
				.setActionRow(mainControls(fluctlight))
				.queue();
	}

	private void performActionDirect(IReplyCallback reply, Fluctlight fluctlight, String action, String value) {
		Optional<String> error = executeAction(action, value);
		if (error.isPresent()) {
			reply.replyEmbeds(buildError(fluctlight, value).build()).setEphemeral(true).queue();
			return;
		}

		// Success: show the category view for this action
		renderCategory(reply, fluctlight, action, false);
	}

	private void performActionAndReport(ButtonInteractionEvent event, Fluctlight fluctlight, String action, String value) {
		Optional<String> error = executeAction(action, value);
		if (error.isPresent()) {
			event.getHook().editOriginalEmbeds(buildError(fluctlight, value).build()).setComponents().queue();
			return;
		}

		EmbedBuilder embed = buildCategoryEmbed(action, fluctlight);
		List<Button> buttons = buildCategoryButtons(action);
		List<ActionRow> rows = toRows(buttons, true, fluctlight, action);
		event.getHook().editOriginalEmbeds(embed.build()).setComponents(rows).queue();
	}

	private Optional<String> executeAction(String action, String value) {
		return switch (action) {
			case "enable" -> attempt(() -> pluginManager.enable(value));
			case "disable" -> attempt(() -> pluginManager.disable(value));
			case "reload" -> attempt(() -> pluginManager.reload(value));
			default -> Optional.of("unsupported");
		};
	}

	private EmbedBuilder buildError(Fluctlight fluctlight, String value) {
		return StyleKit.embeds().error().setTitle(
			Translatable.text("commands.plugin.action.errorTitle")
				.with("actionName", value)
				.resolve(fluctlight)
		);
	}

	private void renderCategory(IReplyCallback event, Fluctlight fluctlight, String action, boolean showBack) {
		EmbedBuilder embed = buildCategoryEmbed(action, fluctlight);
		List<Button> buttons = buildCategoryButtons(action);

		List<ActionRow> rows = toRows(buttons, showBack, fluctlight, action);
		if (event instanceof ButtonInteractionEvent btn) {
			btn.getHook().editOriginalEmbeds(embed.build())
					.setComponents(rows)
					.queue();
			return;
		}

		if (buttons.isEmpty()) {
			event.replyEmbeds(embed.build())
					.setEphemeral(true)
					.queue();
			return;
		}

		event.replyEmbeds(embed.build())
				.setEphemeral(true)
				.setComponents(rows)
				.queue();
	}

	private List<ActionRow> toRows(List<Button> buttons, boolean showBack, Fluctlight fluctlight, String action) {
		List<ActionRow> rows = new ArrayList<>();
		for (int i = 0; i < buttons.size(); i += BUTTONS_PER_ROW) {
			int end = Math.min(i + BUTTONS_PER_ROW, buttons.size());
			rows.add(ActionRow.of(buttons.subList(i, end)));
		}

		// The rescan view can rescan again
		if (RESCAN_ACTION.equals(action)) {
			Button rescan = Components.button(ButtonStyle.PRIMARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.rescan").resolve(fluctlight), RESCAN_ACTION).getButton();
			if (showBack) {
				Button back = Components.button(ButtonStyle.SUCCESS, BACK_LISTENER, Translatable.text("words.back").resolve(fluctlight));
				rows.add(ActionRow.of(rescan, back));
			} else rows.add(ActionRow.of(rescan));
		} else if (showBack) {
			rows.add(ActionRow.of(Components.button(ButtonStyle.SUCCESS, BACK_LISTENER, Translatable.text("words.back").resolve(fluctlight))));
		}

		return rows;
	}

	@ComponentListener(RELOAD_ALL_LISTENER)
	public void onReloadAll(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		long userId = event.getUser().getIdLong();
		Optional<Fluctlight> fluctlightOpt = fluctlightService.get(userId);
		if (fluctlightOpt.isEmpty()) {
			return;
		}
		Fluctlight fluctlight = fluctlightOpt.get();
		long fluctlightId = fluctlight.getId();

		EmbedBuilder embed = StyleKit.embeds().warning();
		embed.setTitle(Translatable.text("commands.plugin.action.reload.confirmation.title").resolve(fluctlightId));
		embed.setDescription(Translatable.text("commands.plugin.action.reload.confirmation.description").resolve(fluctlightId));

		Button confirm = Components.button(ButtonStyle.DANGER, RELOAD_ALL_CONFIRM_LISTENER, Translatable.text("words.confirm").resolve(fluctlightId));
		Button cancel = Components.button(ButtonStyle.SECONDARY, RELOAD_ALL_CANCEL_LISTENER, Translatable.text("words.cancel").resolve(fluctlightId));

		event.getHook()
				.editOriginalEmbeds(embed.build())
				.setActionRow(confirm, cancel)
				.queue();
	}

	@ComponentListener(RELOAD_ALL_CONFIRM_LISTENER)
	public void onReloadAllConfirm(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		long userId = event.getUser().getIdLong();
		Optional<Fluctlight> fluctlightOpt = fluctlightService.get(userId);
		if (fluctlightOpt.isEmpty()) {
			return;
		}
		Fluctlight fluctlight = fluctlightOpt.get();
		long fluctlightId = fluctlight.getId();

		CompletableFuture.runAsync(() -> {
			try {
				for (InstalledExtension plugin : pluginManager.plugins())
					if (plugin.getState() == ExtensionState.ENABLED) pluginManager.reload(plugin.getId());

				// After successful reload, show the main embed with category controls
				event.getHook()
						.editOriginalEmbeds(buildMainEmbed(fluctlight).build())
						.setActionRow(mainControls(fluctlight))
						.queue();
			} catch (Exception e) {
				EmbedBuilder error = StyleKit.embeds().error();
				error.setTitle(Translatable.text("commands.plugin.action.reload.error.title").resolve(fluctlightId));
				error.setDescription(Translatable.text("commands.plugin.action.reload.error.description").resolve(fluctlightId));

				event.getHook()
						.editOriginalEmbeds(error.build())
						.setComponents()
						.queue();
			}
		});
	}

	@ComponentListener(RELOAD_ALL_CANCEL_LISTENER)
	public void onReloadAllCancel(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		long userId = event.getUser().getIdLong();
		Optional<Fluctlight> fluctlightOpt = fluctlightService.get(userId);
		long fluctlightId = fluctlightOpt.map(Fluctlight::getId).orElse(userId);

		EmbedBuilder cancelled = StyleKit.embeds().secondary();
		cancelled.setTitle(Translatable.text("commands.plugin.action.reload.cancelled.title").resolve(fluctlightId));
		cancelled.setDescription(Translatable.text("commands.plugin.action.reload.cancelled.description").resolve(fluctlightId));

		event.getHook()
				.editOriginalEmbeds(cancelled.build())
				.setComponents()
				.queue();
	}

	private EmbedBuilder buildMainEmbed(Fluctlight fluctlight) {
		EmbedBuilder embed = StyleKit.embeds().primary();
		embed.setTitle(Translatable.text("commands.plugin.main.title").resolve(fluctlight));
		embed.setDescription(Translatable.text("commands.plugin.main.description").resolve(fluctlight));

		List<InstalledExtension> all = pluginManager.plugins().stream()
				.sorted(Comparator.comparing(plugin -> Category.name(plugin).toLowerCase(Locale.ROOT)))
				.toList();
		field(embed, fluctlight, "enabled", all.stream().filter(plugin -> plugin.getState() == ExtensionState.ENABLED).toList());
		field(embed, fluctlight, "disabled", all.stream().filter(plugin -> plugin.getState() == ExtensionState.DISABLED).toList());
		field(embed, fluctlight, "waiting", all.stream()
				.filter(plugin -> plugin.getState() == ExtensionState.WAITING || plugin.getState() == ExtensionState.FAILED)
				.toList());

		return embed;
	}

	private void field(EmbedBuilder embed, Fluctlight fluctlight, String state, List<InstalledExtension> plugins) {
		if (plugins.isEmpty()) return;

		String format = Translatable.text("commands.plugin.main.format").resolve(fluctlight);
		List<String> lines = new ArrayList<>();
		for (int i = 0; i < plugins.size(); i++) lines.add(Category.line(format, i + 1, plugins.get(i)).replace("\n", "\n    "));

		String heading = Translatable.text("commands.plugin.main.fields." + state).with("count", plugins.size()).resolve(fluctlight);
		embed.addField(heading, shorten(String.join("\n", lines)), true);
	}

	private Button[] mainControls(Fluctlight fluctlight) {
		PayloadButton enable = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.enable").resolve(fluctlight), "enable");
		PayloadButton disable = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.disable").resolve(fluctlight), "disable");
		PayloadButton reload = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.reload").resolve(fluctlight), "reload");
		PayloadButton rescan = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.rescan").resolve(fluctlight), RESCAN_ACTION);

		Button reloadAll = Components.button(ButtonStyle.PRIMARY, RELOAD_ALL_LISTENER, Translatable.text("commands.plugin.controls.reloadAll").resolve(fluctlight));

		return new Button[]{enable.getButton(), disable.getButton(), reload.getButton(), rescan.getButton(), reloadAll};
	}

	private EmbedBuilder buildCategoryEmbed(String action, Fluctlight fluctlight) {
		Category category = Category.of(action);
		if (category == Category.RESCAN) pluginManager.rescan();

		EmbedBuilder embed = StyleKit.embeds().secondary();
		embed.setTitle(Translatable.text(category.getTitleKey()).resolve(fluctlight));

		String content = category.content(pluginManager, fluctlight.getId());
		if (!content.isBlank()) embed.setDescription(content);

		return embed;
	}

	private List<Button> buildCategoryButtons(String action) {
		Category category = Category.of(action);
		if (category == null || !category.listsPlugins()) return List.of();

		List<InstalledExtension> listed = category.candidates(pluginManager);
		List<Button> buttons = new ArrayList<>();
		for (int i = 0; i < listed.size(); i++)
			buttons.add(Components.button(ButtonStyle.SECONDARY, SELECT_LISTENER, (i + 1) + ".", action + '|' + listed.get(i).getId()).getButton());

		return buttons;
	}

	private String shorten(String listing) {
		String[] lines = listing.split("\n");
		if (lines.length <= 5) return listing;

		return String.join("\n", Arrays.copyOf(lines, 5)) + "\n...";
	}

	private static Optional<String> attempt(Change change) {
		try {
			change.run();
			return Optional.empty();
		} catch (ExtensionException exception) {
			return Optional.of(String.valueOf(exception.getMessage()));
		}
	}

	@FunctionalInterface
	private interface Change {
		InstalledExtension run() throws ExtensionException;
	}
}
