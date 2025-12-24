package me.whereareiam.yui.adapter.command.executor;

import com.sun.management.OperatingSystemMXBean;
import lombok.AllArgsConstructor;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.model.plugin.Plugin;
import me.whereareiam.yui.persistence.LanguagePersistence;
import me.whereareiam.yui.plugin.PluginManager;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.*;

@Component
@AllArgsConstructor
public class StatusCommand {
	private final PluginManager pluginManager;
	private final LanguagePersistence languagePersistence;
	private final ObjectProvider<Settings> settingsProvider;

	@Definition("status")
	@Command("status")
	public void onCommand(Interaction interaction) {
		Fluctlight fluctlight = interaction.fluctlight();

		EmbedBuilder embed = StyleKit.embeds().info();
		embed.setTitle(Translatable.text("commands.status.overview.title").resolve(fluctlight));
		embed.setDescription(buildDescription(fluctlight));

		addMemoryField(embed, fluctlight);
		addCpuField(embed, fluctlight);
		addJavaField(embed, fluctlight);
		addOsField(embed, fluctlight);
		addLocaleField(embed, fluctlight);

		interaction.replyCallback()
				.replyEmbeds(embed.build())
				.setEphemeral(true)
				.queue();
	}

	private void addMemoryField(EmbedBuilder embed, Fluctlight fluctlight) {
		Runtime runtime = Runtime.getRuntime();
		long usedBytes = runtime.totalMemory() - runtime.freeMemory();
		long allocatedBytes = runtime.totalMemory();
		long maxBytes = runtime.maxMemory();
		int percent = allocatedBytes > 0 ? (int) Math.round((usedBytes * 100.0) / allocatedBytes) : 0;

		String title = Translatable.text("commands.status.overview.fields.memory.title").resolve(fluctlight);
		Map<String, Object> placeholders = new HashMap<>();
		placeholders.put("used", formatBytes(usedBytes));
		placeholders.put("allocated", formatBytes(allocatedBytes));
		placeholders.put("percent", percent);
		placeholders.put("max", formatBytes(maxBytes));

		String value = resolveLines("commands.status.overview.fields.memory.value", placeholders, fluctlight);

		embed.addField(title, value, true);
	}

	private void addCpuField(EmbedBuilder embed, Fluctlight fluctlight) {
		String processLoad = resolveProcessLoad();

		int cores = Runtime.getRuntime().availableProcessors();

		String title = Translatable.text("commands.status.overview.fields.cpu.title").resolve(fluctlight);
		Map<String, Object> placeholders = new HashMap<>();
		placeholders.put("process", processLoad);
		placeholders.put("cores", cores);

		String value = resolveLines("commands.status.overview.fields.cpu.value", placeholders, fluctlight);

		embed.addField(title, value, true);
	}

	private void addJavaField(EmbedBuilder embed, Fluctlight fluctlight) {
		String runtimeName = System.getProperty("java.runtime.name");
		String runtimeVersion = System.getProperty("java.runtime.version");
		String runtime = joinNonBlank(runtimeName, runtimeVersion, System.getProperty("java.version"));
		String vendor = System.getProperty("java.vendor");
		String vm = System.getProperty("java.vm.name");

		String title = Translatable.text("commands.status.overview.fields.java.title").resolve(fluctlight);
		Map<String, Object> placeholders = new HashMap<>();
		placeholders.put("runtime", nullToFallback(runtime));
		placeholders.put("vendor", nullToFallback(vendor));
		placeholders.put("vm", nullToFallback(vm));

		String value = resolveLines("commands.status.overview.fields.java.value", placeholders, fluctlight);

		embed.addField(title, value, false);
	}

	private void addOsField(EmbedBuilder embed, Fluctlight fluctlight) {
		String name = System.getProperty("os.name");
		String version = System.getProperty("os.version");
		String arch = System.getProperty("os.arch");

		String title = Translatable.text("commands.status.overview.fields.os.title").resolve(fluctlight);
		String osValue = joinNonBlank(name, version, arch);
		Map<String, Object> placeholders = new HashMap<>();
		placeholders.put("system", nullToFallback(osValue));

		String value = resolveLines("commands.status.overview.fields.os.value", placeholders, fluctlight);

		embed.addField(title, value, false);
	}

	private void addLocaleField(EmbedBuilder embed, Fluctlight fluctlight) {
		DiscordLocale defaultLocale = settingsProvider.getObject().getLocale();
		Collection<DiscordLocale> availableLocales = languagePersistence.getAvailableLanguages();
		List<DiscordLocale> available = availableLocales == null ? new ArrayList<>() : new ArrayList<>(availableLocales);
		available.removeIf(Objects::isNull);
		available.sort(Comparator.comparing(DiscordLocale::getLocale));

		String title = Translatable.text("commands.status.overview.fields.locale.title").resolve(fluctlight);
		String defaultValue = defaultLocale != null ? defaultLocale.getLocale() : "N/A";
		String availableValue = available.isEmpty()
				? "N/A"
				: String.join(", ", available.stream().map(DiscordLocale::getLocale).toList());

		Map<String, Object> placeholders = new HashMap<>();
		placeholders.put("default", defaultValue);
		placeholders.put("available", availableValue);

		String value = resolveLines("commands.status.overview.fields.locale.value", placeholders, fluctlight);

		embed.addField(title, value, false);
	}

	private String buildDescription(Fluctlight fluctlight) {
		String template = Translatable.text("commands.status.overview.description").resolve(fluctlight);
		String plugins = buildPluginsBlock(fluctlight);
		if (template == null || template.isBlank()) return plugins;
		if (template.contains("<plugins>")) return template.replace("<plugins>", plugins).trim();
		return (template + "\n" + plugins).trim();
	}

	private String buildPluginsBlock(Fluctlight fluctlight) {
		String title = Translatable.text("commands.status.overview.plugins.title").resolve(fluctlight);
		String statusEnabled = Translatable.text("commands.status.overview.plugins.status.enabled").resolve(fluctlight);
		String statusDisabled = Translatable.text("commands.status.overview.plugins.status.disabled").resolve(fluctlight);
		String statusLoadable = Translatable.text("commands.status.overview.plugins.status.loadable").resolve(fluctlight);
		String format = Translatable.text("commands.status.overview.plugins.format").resolve(fluctlight);

		List<String> lines = new ArrayList<>();

		List<InternalPlugin> enabled = pluginManager.plugins().stream()
				.filter(InternalPlugin::isEnabled)
				.sorted(Comparator.comparing(p -> safeLowerName(p.getPlugin().getName(), p.getPlugin().getId())))
				.toList();
		for (InternalPlugin plugin : enabled) {
			addPluginLine(lines, formatPluginLine(
					format,
					plugin.getPlugin().getName(),
					plugin.getPlugin().getVersion(),
					plugin.getPlugin().getAuthors(),
					plugin.getPlugin().getId(),
					null,
					statusEnabled
			));
		}

		List<InternalPlugin> disabled = pluginManager.plugins().stream()
				.filter(p -> !p.isEnabled())
				.sorted(Comparator.comparing(p -> safeLowerName(p.getPlugin().getName(), p.getPlugin().getId())))
				.toList();
		for (InternalPlugin plugin : disabled) {
			addPluginLine(lines, formatPluginLine(
					format,
					plugin.getPlugin().getName(),
					plugin.getPlugin().getVersion(),
					plugin.getPlugin().getAuthors(),
					plugin.getPlugin().getId(),
					null,
					statusDisabled
			));
		}

		List<Map.Entry<String, Plugin>> loadable = new ArrayList<>(pluginManager.loadable().entrySet());
		loadable.sort(Comparator.comparing(entry -> safeLowerName(entry.getValue().getName(), entry.getKey())));
		for (Map.Entry<String, Plugin> entry : loadable) {
			Plugin plugin = entry.getValue();
			addPluginLine(lines, formatPluginLine(
					format,
					plugin.getName(),
					plugin.getVersion(),
					plugin.getAuthors(),
					null,
					entry.getKey(),
					statusLoadable
			));
		}

		if (lines.isEmpty()) {
			lines.add(Translatable.text("commands.status.overview.plugins.empty").resolve(fluctlight));
		}

		return title + "\n" + String.join("\n", lines);
	}

	private String formatPluginLine(
			String format,
			String name,
			String version,
			List<String> authors,
			String id,
			String jarBase,
			String statusLabel
	) {
		String displayName = resolvePluginName(name, id, jarBase);

		String versionToken = (version == null || version.isBlank()) ? "" : version;

		String authorsText = "";
		if (authors != null && !authors.isEmpty()) {
			String joined = String.join(", ", authors);
			if (!joined.isBlank()) authorsText = joined;
		}

		String idToken = (id == null || id.isBlank()) ? "" : id;
		String jarToken = (jarBase == null || jarBase.isBlank()) ? "" : jarBase;
		String statusToken = statusLabel == null ? "" : statusLabel;

		Map<String, String> tokens = new HashMap<>();
		tokens.put("name", displayName);
		tokens.put("version", versionToken);
		tokens.put("authors", authorsText);
		tokens.put("id", idToken);
		tokens.put("jar", jarToken);
		tokens.put("status", statusToken);

		String rendered = format;
		if (rendered == null || rendered.isBlank()) return "";

		rendered = replaceTokens(rendered, tokens);

		return normalizeSpaces(rendered);
	}

	private void addPluginLine(List<String> lines, String line) {
		if (line == null || line.isBlank()) return;
		lines.add(line);
	}

	private String normalizeSpaces(String value) {
		if (value == null) return "";
		return value.replaceAll("\\s{2,}", " ").trim();
	}

	private String resolveLines(String key, Map<String, Object> placeholders, Fluctlight fluctlight) {
		return Translatable.text(key)
				.with(placeholders)
				.resolve(fluctlight);
	}

	private String replaceTokens(String format, Map<String, String> tokens) {
		String result = format;
		for (Map.Entry<String, String> entry : tokens.entrySet()) {
			String token = "<" + entry.getKey() + ">";
			String value = entry.getValue() == null ? "" : entry.getValue();
			result = result.replace(token, value);
		}
		return result;
	}

	private OperatingSystemMXBean getOsBean() {
		java.lang.management.OperatingSystemMXBean base = ManagementFactory.getOperatingSystemMXBean();
		if (!(base instanceof OperatingSystemMXBean osBean)) return null;
		return osBean;
	}

	private String formatBytes(long bytes) {
		if (bytes < 0) return "N/A";

		final long kb = 1024;
		final long mb = kb * 1024;
		final long gb = mb * 1024;

		if (bytes >= gb) return String.format("%.2f GB", bytes / (double) gb);
		if (bytes >= mb) return String.format("%.2f MB", bytes / (double) mb);
		if (bytes >= kb) return String.format("%.2f KB", bytes / (double) kb);

		return bytes + " B";
	}


	private String joinNonBlank(String primary, String secondary, String fallback) {
		String value = joinNonBlank(primary, secondary);
		return (value == null || value.isBlank()) ? fallback : value;
	}

	private String joinNonBlank(String first, String second) {
		StringBuilder sb = new StringBuilder();
		if (first != null && !first.isBlank()) sb.append(first);
		if (second != null && !second.isBlank()) {
			if (!sb.isEmpty()) sb.append(" ");
			sb.append(second);
		}
		String result = sb.toString();
		if (result.isBlank()) return null;
		return result;
	}

	private String nullToFallback(String value) {
		return (value == null || value.isBlank()) ? "N/A" : value;
	}

	private String safeLowerName(String name, String fallback) {
		String value = (name == null || name.isBlank()) ? fallback : name;
		if (value == null) return "";
		return value.toLowerCase(Locale.ROOT);
	}

	private String resolvePluginName(String name, String id, String jarBase) {
		if (name != null && !name.isBlank()) return name;
		if (jarBase != null && !jarBase.isBlank()) return jarBase;
		if (id != null && !id.isBlank()) return id;
		return "Unknown";
	}

	private String resolveProcessLoad() {
		OperatingSystemMXBean osBean = getOsBean();
		if (osBean == null) return "N/A";
		double load = osBean.getProcessCpuLoad();
		if (load < 0) return "N/A";
		return String.format(Locale.ROOT, "%.1f%%", load * 100.0);
	}
}
