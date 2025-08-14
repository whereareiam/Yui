package me.whereareiam.yui.adapter.command.executor.plugin;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.api.annotation.Command;
import me.whereareiam.yui.api.annotation.ComponentListener;
import me.whereareiam.yui.api.model.PayloadButton;
import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import me.whereareiam.yui.api.model.plugin.Plugin;
import me.whereareiam.yui.api.output.CommandBase;
import me.whereareiam.yui.api.output.plugin.PluginManager;
import me.whereareiam.yui.api.style.StyleKit;
import me.whereareiam.yui.api.util.Components;
import me.whereareiam.yui.api.util.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@AllArgsConstructor
public class PluginCommand implements CommandBase {
	private final PluginManager pluginManager;

	private static final String CATEGORY_LISTENER = "command_plugin_category";
	private static final String SELECT_LISTENER = "command_plugin_select";
	private static final String BACK_LISTENER = "command_plugin_back";
	private static final String LOAD_ACTION = "load";

	private static final int BUTTONS_PER_ROW = 5;

	@Command(name = "plugin")
	public void onCommand(SlashCommandInteractionEvent event) {
		String action = optLower(event.getOption("action"));
		String pluginArg = optRaw(event.getOption("plugin"));

		if (action == null) {
			event.replyEmbeds(buildMainEmbed(event.getUser().getIdLong()).build())
					.setEphemeral(true)
					.addActionRow(mainControls(event.getUser().getIdLong()))
					.queue();
			return;
		}

		if (!Category.isSupported(action)) {
			event.replyEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.error.validation.invalidButton", event.getUser().getIdLong()))
							.build())
					.setEphemeral(true)
					.queue();
			return;
		}

		if (pluginArg == null) {
			renderCategory(event, action, false);
			return;
		}

		performActionDirect(event, action, pluginArg);
	}

	@ComponentListener(CATEGORY_LISTENER)
	public void onCategory(ButtonInteractionEvent event) {
		String payload = Components.payload(event);
		if (payload == null) {
			event.deferEdit().queue();
			event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.error.validation.invalidButton", event.getUser().getIdLong()))
							.build())
					.setComponents()
					.queue();
			return;
		}

		event.deferEdit().queue();
		renderCategory(event, payload, true);
	}

	@ComponentListener(SELECT_LISTENER)
	public void onSelect(ButtonInteractionEvent event) {
		String payload = Components.payload(event);
		if (payload == null || !payload.contains("|")) {
			event.deferEdit().queue();
			event.getHook().editOriginalEmbeds(StyleKit.embeds().error()
							.setTitle(Translatable.of("commands.error.validation.invalidButton", event.getUser().getIdLong()))
							.build())
					.setComponents()
					.queue();
			return;
		}

		String action = payload.substring(0, payload.indexOf('|'));
		String value = payload.substring(payload.indexOf('|') + 1);

		event.deferEdit().queue();
		performActionAndReport(event, action, value);
	}

	@ComponentListener(BACK_LISTENER)
	public void onBack(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		long userId = event.getUser().getIdLong();

		event.getHook()
				.editOriginalEmbeds(buildMainEmbed(userId).build())
				.setActionRow(mainControls(userId))
				.queue();
	}

	private void performActionDirect(SlashCommandInteractionEvent event, String action, String value) {
		long userId = event.getUser().getIdLong();
		Optional<String> error = executeAction(action, value);
		if (error.isPresent()) {
			event.replyEmbeds(buildError(userId, value).build()).setEphemeral(true).queue();
			return;
		}

		// Success: show the category view for this action
		renderCategory(event, action, false);
	}

	private void performActionAndReport(ButtonInteractionEvent event, String action, String value) {
		long userId = event.getUser().getIdLong();
		Optional<String> error = executeAction(action, value);
		if (error.isPresent()) {
			event.getHook().editOriginalEmbeds(buildError(userId, value).build()).setComponents().queue();
			return;
		}

		EmbedBuilder embed = buildCategoryEmbed(action, userId);
		List<Button> buttons = buildCategoryButtons(action);
		List<ActionRow> rows = toRows(buttons, true, userId, action);
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

	private EmbedBuilder buildError(long userId, String value) {
		return StyleKit.embeds().error().setTitle(Translatable.forUser("commands.plugin.action.errorTitle", userId, value));
	}

	private void renderCategory(IReplyCallback event, String action, boolean showBack) {
		long userId = event.getUser().getIdLong();
		EmbedBuilder embed = buildCategoryEmbed(action, userId);
		List<Button> buttons = buildCategoryButtons(action);

		List<ActionRow> rows = toRows(buttons, showBack, userId, action);
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

	private List<ActionRow> toRows(List<Button> buttons, boolean showBack, long userId, String action) {
		List<ActionRow> rows = new ArrayList<>();
		for (int i = 0; i < buttons.size(); i += BUTTONS_PER_ROW) {
			int end = Math.min(i + BUTTONS_PER_ROW, buttons.size());
			rows.add(ActionRow.of(buttons.subList(i, end)));
		}

		// Extra controls for the load category
		if (LOAD_ACTION.equals(action)) {
			Button reload = Components.button(ButtonStyle.PRIMARY, CATEGORY_LISTENER, Translatable.of("commands.plugin.controls.reload", userId), "load").getButton();
			if (showBack) {
				Button back = Components.button(ButtonStyle.SUCCESS, BACK_LISTENER, Translatable.of("vocabulary.back", userId));
				rows.add(ActionRow.of(reload, back));
			} else rows.add(ActionRow.of(reload));
		} else if (showBack) {
			rows.add(ActionRow.of(Components.button(ButtonStyle.SUCCESS, BACK_LISTENER, Translatable.of("vocabulary.back", userId))));
		}

		return rows;
	}

	private EmbedBuilder buildMainEmbed(long userId) {
		EmbedBuilder embed = StyleKit.embeds().primary();
		embed.setTitle(Translatable.of("commands.plugin.main.title", userId));
		embed.setDescription(Translatable.of("commands.plugin.main.description", userId));

		List<InternalPlugin> all = pluginManager.plugins().stream()
				.sorted(Comparator.comparing(p -> p.getPlugin().getName().toLowerCase(Locale.ROOT)))
				.toList();

		List<InternalPlugin> enabled = all.stream().filter(InternalPlugin::isEnabled).toList();
		List<InternalPlugin> disabled = all.stream().filter(p -> !p.isEnabled()).toList();
		Map<String, Plugin> loadable = pluginManager.loadable();

		if (!enabled.isEmpty()) {
			String listing = enumeratePluginsMain(enabled, userId);
			listing = shorten(listing);
			String heading = Translatable.forUser("commands.plugin.main.fields.enabled", userId, enabled.size());
			embed.addField(heading, listing, true);
		}

		if (!disabled.isEmpty()) {
			String listing = enumeratePluginsMain(disabled, userId);
			listing = shorten(listing);
			String heading = Translatable.forUser("commands.plugin.main.fields.disabled", userId, disabled.size());
			embed.addField(heading, listing, true);
		}

		if (!loadable.isEmpty()) {
			String listing = enumerateLoadable(loadable, userId);
			listing = shorten(listing);
			String heading = Translatable.forUser("commands.plugin.main.fields.loadable", userId, loadable.size());
			embed.addField(heading, listing, true);
		}

		return embed;
	}

	private Button[] mainControls(long userId) {
		PayloadButton enable = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.of("commands.plugin.controls.enable", userId), "enable");
		PayloadButton disable = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.of("commands.plugin.controls.disable", userId), "disable");
		PayloadButton load = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.of("commands.plugin.controls.load", userId), "load");
		PayloadButton unload = Components.button(ButtonStyle.SECONDARY, CATEGORY_LISTENER, Translatable.of("commands.plugin.controls.unload", userId), "unload");

		return new Button[]{enable.getButton(), disable.getButton(), load.getButton(), unload.getButton()};
	}

	private EmbedBuilder buildCategoryEmbed(String action, long userId) {
		if (LOAD_ACTION.equals(action)) {
			String titleKey = "commands.plugin.load.title";
			String content = buildLoadContent(userId);
			return buildCategoryEmbed(userId, titleKey, content);
		}

		Category category = Category.of(action);
		String titleKey = category.getTitleKey();
		String content = category.content(pluginManager, userId);

		return buildCategoryEmbed(userId, titleKey, content);
	}

	private EmbedBuilder buildCategoryEmbed(long userId, String titleKey, String content) {
		EmbedBuilder embed = StyleKit.embeds().secondary();
		embed.setTitle(Translatable.of(titleKey, userId));

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

	private String buildLoadContent(long userId) {
		List<Map.Entry<String, Plugin>> loadable = sortedLoadable();
		if (loadable.isEmpty()) return Translatable.of("commands.plugin.load.empty", userId);
		String format = Translatable.of("commands.plugin.load.format", userId);
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
		String template = Translatable.of("commands.plugin.load.description", userId);

		return template.replace("{list}", list);
	}

	private String enumerateLoadable(Map<String, Plugin> loadable, long userId) {
		List<Map.Entry<String, Plugin>> entries = new ArrayList<>(loadable.entrySet());
		entries.sort(Comparator.comparing(e -> {
			String name = e.getValue().getName();
			return name == null ? e.getKey().toLowerCase(Locale.ROOT) : name.toLowerCase(Locale.ROOT);
		}));
		String format = Translatable.of("commands.plugin.load.format", userId);
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

	private String enumeratePluginsMain(List<InternalPlugin> list, long userId) {
		String format = Translatable.of("commands.plugin.main.format", userId);
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

	private static String optLower(OptionMapping opt) {
		if (opt == null) return null;
		String v = opt.getAsString();

		return v.toLowerCase(Locale.ROOT).trim();
	}

	private static String optRaw(OptionMapping opt) {
		return opt == null ? null : opt.getAsString();
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


