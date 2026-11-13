package me.whereareiam.yui.common.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.semantica.source.Source;
import me.whereareiam.yui.message.MessageDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Default texts from {@link MessageDefaults}, in English, kept in memory: they are never written into
 * {@code messages/}, which may be read-only, and the files there override them key by key.
 */
final class DefaultsSource implements Source {
	static final Locale ENGLISH = Locale.forLanguageTag("en-US");

	private final String name;
	private final String prefix;
	private final List<? extends MessageDefaults<?>> defaults;
	private final ObjectMapper mapper;

	/**
	 * @param name     names the source in issues, such as {@code yui:defaults}
	 * @param prefix   put before every key, such as a plugin's id, or empty
	 * @param defaults the files of default texts
	 * @param mapper   reads the models as Configura reads files
	 */
	DefaultsSource(String name, String prefix, List<? extends MessageDefaults<?>> defaults, ObjectMapper mapper) {
		this.name = name;
		this.prefix = prefix.isEmpty() ? "" : prefix + ".";
		this.defaults = List.copyOf(defaults);
		this.mapper = mapper;
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	@Override
	public @NotNull Content load() {
		Content.Builder content = Content.builder();
		for (MessageDefaults<?> file : defaults)
			put(content, prefix + file.file(), mapper.valueToTree(file.defaults()), name + ":" + file.file());

		return content.build();
	}

	private static void put(Content.Builder content, String key, JsonNode node, String origin) {
		if (node.isTextual()) {
			content.put(ENGLISH, key, node.textValue(), origin);
			return;
		}

		if (node.isArray()) {
			StringJoiner lines = new StringJoiner("\n");
			node.forEach(line -> lines.add(line.asText()));
			content.put(ENGLISH, key, lines.toString(), origin);
			return;
		}

		for (Iterator<Map.Entry<String, JsonNode>> fields = node.fields(); fields.hasNext(); ) {
			Map.Entry<String, JsonNode> field = fields.next();
			put(content, key + "." + field.getKey(), field.getValue(), origin);
		}
	}
}
