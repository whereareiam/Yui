package me.whereareiam.yui.common.config.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import me.whereareiam.strata.adapter.configura.ConfigContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * {@code messages/}, migration 1: the texts of {@code languages/} move into one folder per locale, a file per config
 * file they speak for: {@code commands.yml}, {@code audit.yml}, {@code conversations.yml} (once
 * {@code general.conversation}) and {@code words.yml} (once {@code vocabulary.yml}, every locale in one file).
 * Placeholders become ICU arguments ({@code <p:seconds>} is {@code {seconds}}).
 *
 * <p>A text moved here overrides Yui's default from then on; an {@code en-US} file that only repeats the defaults an
 * older Yui wrote can be deleted, to follow new defaults.</p>
 */
final class MessagesMigration {
	private static final Pattern PLACEHOLDER = Pattern.compile("<p:([A-Za-z][A-Za-z0-9_]*)>");
	private static final Pattern LOCALE = Pattern.compile("[a-z]{2,3}(-[A-Za-z0-9]+)*");
	private static final Map<String, String> SECTIONS = Map.of("commands", "commands", "audit", "audit");

	private final Path folder;

	MessagesMigration(Path folder) {
		this.folder = folder;
	}

	void apply(ConfigContext files) {
		for (String locale : locales()) {
			String old = "languages/" + locale + ".yml";
			ObjectNode texts = files.document(old);
			SECTIONS.forEach((section, file) -> move(files, texts.get(section), "messages/" + locale + "/" + file + ".yml"));
			move(files, texts.path("general").get("conversation"), "messages/" + locale + "/conversations.yml");
			files.delete(old);
		}

		if (files.exists("languages/vocabulary.yml")) {
			words(files, files.document("languages/vocabulary.yml"));
			files.delete("languages/vocabulary.yml");
		}

		files.deleteDirectoryIfEmpty("languages");
	}

	private List<String> locales() {
		Path languages = folder.resolve("languages");
		if (!Files.isDirectory(languages)) return List.of();

		try (Stream<Path> children = Files.list(languages)) {
			return children.map(path -> path.getFileName().toString())
					.filter(name -> name.endsWith(".yml") && !name.equals("vocabulary.yml"))
					.map(name -> name.substring(0, name.length() - ".yml".length()))
					.toList();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	private static void move(ConfigContext files, JsonNode section, String target) {
		if (!(section instanceof ObjectNode texts) || texts.isEmpty()) return;

		files.document(target).setAll((ObjectNode) icu(texts));
	}

	// vocabulary.yml: cancel: { en-US: Cancel, de: Abbrechen } becomes cancel: Cancel in messages/en-US/words.yml.
	private static void words(ConfigContext files, JsonNode vocabulary) {
		Map<String, ObjectNode> byLocale = new LinkedHashMap<>();
		collect(vocabulary, List.of(), byLocale);
		byLocale.forEach((locale, words) -> files.document("messages/" + locale + "/words.yml").setAll(words));
	}

	private static void collect(JsonNode node, List<String> path, Map<String, ObjectNode> byLocale) {
		for (Iterator<Map.Entry<String, JsonNode>> fields = node.fields(); fields.hasNext(); ) {
			Map.Entry<String, JsonNode> field = fields.next();
			List<String> key = new ArrayList<>(path);
			key.add(field.getKey());

			if (!translations(field.getValue())) {
				collect(field.getValue(), key, byLocale);
				continue;
			}

			field.getValue().fields().forEachRemaining(translation -> {
				ObjectNode words = byLocale.computeIfAbsent(translation.getKey(), ignored -> JsonNodeFactory.instance.objectNode());
				for (String segment : key.subList(0, key.size() - 1)) words = words.withObjectProperty(segment);
				words.set(key.getLast(), icu(translation.getValue()));
			});
		}
	}

	// A map of locale to text, such as { en-US: Cancel, de: Abbrechen }.
	private static boolean translations(JsonNode value) {
		if (!value.isObject() || value.isEmpty()) return false;

		for (Iterator<Map.Entry<String, JsonNode>> fields = value.fields(); fields.hasNext(); ) {
			Map.Entry<String, JsonNode> field = fields.next();
			if (!field.getValue().isTextual() || !LOCALE.matcher(field.getKey()).matches()) return false;
		}

		return true;
	}

	private static JsonNode icu(JsonNode node) {
		if (node.isTextual()) return TextNode.valueOf(PLACEHOLDER.matcher(node.textValue()).replaceAll("{$1}"));

		if (node.isArray()) {
			ArrayNode lines = JsonNodeFactory.instance.arrayNode();
			node.forEach(line -> lines.add(icu(line)));
			return lines;
		}

		if (!node.isObject()) return node;

		ObjectNode object = JsonNodeFactory.instance.objectNode();
		node.fields().forEachRemaining(field -> object.set(field.getKey(), icu(field.getValue())));
		return object;
	}
}
