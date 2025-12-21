package me.whereareiam.yui.adapter.command.executor.plugin;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.model.PayloadButton;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.model.plugin.Plugin;
import me.whereareiam.yui.plugin.PluginManager;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.translation.Translatable;
import me.whereareiam.yui.util.Components;
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
	private static final String LOAD_ACTION = "load";

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
			case "enable" -> tryEnable(value);
			case "disable" -> tryDisable(value);
			case "unload" -> tryUnload(value);
			case LOAD_ACTION -> tryLoad(value);
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

		// Extra controls for the load category
		if (LOAD_ACTION.equals(action)) {
			Button reload = Components.button(ButtonStyle.PRIMARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.reload").resolve(fluctlight), "load").getButton();
			if (showBack) {
				Button back = Components.button(ButtonStyle.SUCCESS, BACK_LISTENER, Translatable.text("vocabulary.back").resolve(fluctlight));
				rows.add(ActionRow.of(reload, back));
			} else rows.add(ActionRow.of(reload));
		} else if (showBack) {
			rows.add(ActionRow.of(Components.button(ButtonStyle.SUCCESS, BACK_LISTENER, Translatable.text("vocabulary.back").resolve(fluctlight))));
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

		Button confirm = Components.button(ButtonStyle.DANGER, RELOAD_ALL_CONFIRM_LISTENER, Translatable.text("vocabulary.confirm").resolve(fluctlightId));
		Button cancel = Components.button(ButtonStyle.SECONDARY, RELOAD_ALL_CANCEL_LISTENER, Translatable.text("vocabulary.cancel").resolve(fluctlightId));

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
				pluginManager.reload();

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

		List<InternalPlugin> all = pluginManager.plugins().stream()
				.sorted(Comparator.comparing(p -> p.getPlugin().getName().toLowerCase(Locale.ROOT)))
				.toList();

		List<InternalPlugin> enabled = all.stream().filter(InternalPlugin::isEnabled).toList();
		List<InternalPlugin> disabled = all.stream().filter(p -> !p.isEnabled()).toList();
		Map<String, Plugin> loadable = pluginManager.loadable();

		if (!enabled.isEmpty()) {
			String listing = enumeratePluginsMain(enabled, fluctlight);
			listing = shorten(listing);
			String heading = Translatable.text("commands.plugin.main.fields.enabled")
				.with("count", enabled.size())
				.resolve(fluctlight);
			embed.addField(heading, listing, true);
		}

		if (!disabled.isEmpty()) {
			String listing = enumeratePluginsMain(disabled, fluctlight);
			listing = shorten(listing);
			String heading = Translatable.text("commands.plugin.main.fields.disabled")
				.with("count", disabled.size())
				.resolve(fluctlight);
			embed.addField(heading, listing, true);
		}

		if (!loadable.isEmpty()) {
			String listing = enumerateLoadable(loadable, fluctlight);
			listing = shorten(listing);
			String heading = Translatable.text("commands.plugin.main.fields.loadable")
				.with("count", loadable.size())
				.resolve(fluctlight);
			embed.addField(heading, listing, true);
		}

		return embed;
	}

	private Button[] mainControls(Fluctlight fluctlight) {
		PayloadButton enable = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.enable").resolve(fluctlight), "enable");
		PayloadButton disable = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.disable").resolve(fluctlight), "disable");
		PayloadButton load = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.load").resolve(fluctlight), "load");
		PayloadButton unload = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.text("commands.plugin.controls.unload").resolve(fluctlight), "unload");

		Button reloadAll = Components.button(ButtonStyle.PRIMARY, RELOAD_ALL_LISTENER, Translatable.text("commands.plugin.controls.reloadAll").resolve(fluctlight));

		return new Button[]{enable.getButton(), disable.getButton(), load.getButton(), unload.getButton(), reloadAll};
	}

	private EmbedBuilder buildCategoryEmbed(String action, Fluctlight fluctlight) {
		if (LOAD_ACTION.equals(action)) {
			String titleKey = "commands.plugin.load.title";
			String content = buildLoadContent(fluctlight);
			return buildCategoryEmbed(fluctlight, titleKey, content);
		}

		Category category = Category.of(action);
		String titleKey = category.getTitleKey();
		String content = category.content(pluginManager, fluctlight.getId());

		return buildCategoryEmbed(fluctlight, titleKey, content);
	}

	private EmbedBuilder buildCategoryEmbed(Fluctlight fluctlight, String titleKey, String content) {
		EmbedBuilder embed = StyleKit.embeds().secondary();
		embed.setTitle(Translatable.text(titleKey).resolve(fluctlight));

		if (!content.isBlank()) embed.setDescription(content);

		return embed;
	}

	private List<Button> buildCategoryButtons(String action) {
		List<Button> buttons = new ArrayList<>();
		if (LOAD_ACTION.equals(action)) {
			List<Map.Entry<String, Plugin>> loadable = sortedLoadable();
			for (int i = 0; i < loadable.size(); i++) {
				String jarBase = loadable.get(i).getKey();
				buttons.add(Components.button(ButtonStyle.SECONDARY, SELECT_LISTENER, (i + 1) + ".", action + '|' + jarBase).getButton());
			}

			return buttons;
		} else {
			Category category = Category.of(action);
			if (category != null && category.listsPlugins()) {
				List<InternalPlugin> list = category.candidates(pluginManager);
				for (int i = 0; i < list.size(); i++)
					buttons.add(Components.button(ButtonStyle.SECONDARY, SELECT_LISTENER, (i + 1) + ".", action + '|' + list.get(i).getPlugin().getId()).getButton());
			}
		}
		return buttons;
	}

	private List<Map.Entry<String, Plugin>> sortedLoadable() {
		Map<String, Plugin> map = pluginManager.loadable();
		List<Map.Entry<String, Plugin>> entries = new ArrayList<>(map.entrySet());

		entries.sort(Comparator.comparing(e -> {
			String name = e.getValue().getName();
			return name == null ? e.getKey().toLowerCase(Locale.ROOT) : name.toLowerCase(Locale.ROOT);
		}));

		return entries;
	}

	private String buildLoadContent(Fluctlight fluctlight) {
		List<Map.Entry<String, Plugin>> loadable = sortedLoadable();
		if (loadable.isEmpty()) return Translatable.text("commands.plugin.load.empty").resolve(fluctlight);
		String format = Translatable.text("commands.plugin.load.format").resolve(fluctlight);
		List<String> lines = new ArrayList<>();

		for (int i = 0; i < loadable.size(); i++) {
			Plugin p = loadable.get(i).getValue();
			String name = p.getName() == null ? loadable.get(i).getKey() : p.getName();
			String line = format
					.replace("{index}", String.valueOf(i + 1))
					.replace("{name}", name);
			lines.add(line);
		}

		String list = String.join("\n", lines);
		String template = Translatable.text("commands.plugin.load.description").resolve(fluctlight);

		return template.replace("{list}", list);
	}

	private String enumerateLoadable(Map<String, Plugin> loadable, Fluctlight fluctlight) {
		List<Map.Entry<String, Plugin>> entries = new ArrayList<>(loadable.entrySet());
		entries.sort(Comparator.comparing(e -> {
			String name = e.getValue().getName();
			return name == null ? e.getKey().toLowerCase(Locale.ROOT) : name.toLowerCase(Locale.ROOT);
		}));
		String format = Translatable.text("commands.plugin.load.format").resolve(fluctlight);
		List<String> lines = new ArrayList<>();
		for (int i = 0; i < entries.size(); i++) {
			Plugin p = entries.get(i).getValue();
			String name = p.getName() == null ? entries.get(i).getKey() : p.getName();
			String line = format
					.replace("{index}", String.valueOf(i + 1))
					.replace("{name}", name);
			lines.add(line);
		}

		return String.join("\n", lines);
	}

	private String enumeratePluginsMain(List<InternalPlugin> list, Fluctlight fluctlight) {
		String format = Translatable.text("commands.plugin.main.format").resolve(fluctlight);
		List<String> lines = new ArrayList<>();

		for (int i = 0; i < list.size(); i++) {
			InternalPlugin p = list.get(i);
			String authors = p.getPlugin().getAuthors() == null || p.getPlugin().getAuthors().isEmpty()
					? ""
					: String.join(", ", p.getPlugin().getAuthors());
			String authorsPart = authors.isBlank() ? "" : " " + authors;
			String line = format
					.replace("{index}", String.valueOf(i + 1))
					.replace("{name}", safe(p.getPlugin().getName()))
					.replace("{version}", safe(p.getPlugin().getVersion()))
					.replace("{authors}", authorsPart)
					.replace("{id}", safe(p.getPlugin().getId()));
			String indented = line.replace("\n", "\n    ");
			lines.add(indented.trim());
		}

		return String.join("\n", lines);
	}

	private String shorten(String listing) {
		if (listing == null || listing.isBlank()) return listing;
		String[] lines = listing.split("\n");
		if (lines.length <= 5) return listing;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			sb.append(lines[i]);
			if (i < 5 - 1) sb.append("\n");
		}
		sb.append("\n...");
		return sb.toString();
	}

	private static String safe(String v) {
		return v == null ? "" : v;
	}

	private Optional<String> tryEnable(String id) {
		return pluginManager.enable(id).isPresent() ? Optional.empty() : Optional.of("fail");
	}

	private Optional<String> tryDisable(String id) {
		return pluginManager.disable(id).isPresent() ? Optional.empty() : Optional.of("fail");
	}

	private Optional<String> tryUnload(String id) {
		return pluginManager.unload(id).isPresent() ? Optional.empty() : Optional.of("fail");
	}

	private Optional<String> tryLoad(String pluginName) {
		try {
			pluginManager.load(pluginName);
			return Optional.empty();
		} catch (Exception e) {
			return Optional.of(e.getMessage());
		}
	}
}


