package me.whereareiam.yui.common.config.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.strata.adapter.configura.ConfigContext;

import java.util.Iterator;
import java.util.Map;

/**
 * {@code languages.yml}, migration 1: the languages are listed at the top, with an emoji and a role id each; a
 * disabled language is left out, the display name (now a text) and the rendering caches go.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class LanguagesMigration {
	private static final String LANGUAGES = "languages.yml";

	static void apply(ConfigContext files) {
		if (!files.exists(LANGUAGES)) return;

		ObjectNode document = files.document(LANGUAGES);
		JsonNode old = document.remove("languages");
		document.remove("settings");
		if (!(old instanceof ObjectNode entries)) return;

		for (Iterator<Map.Entry<String, JsonNode>> fields = entries.fields(); fields.hasNext(); ) {
			Map.Entry<String, JsonNode> field = fields.next();
			JsonNode entry = field.getValue();
			if (!entry.path("enabled").asBoolean(true)) continue;

			ObjectNode language = document.putObject(field.getKey());
			if (Nodes.present(entry.path("emoji"))) language.set("emoji", entry.get("emoji"));

			JsonNode role = entry.path("role");
			if (role.canConvertToLong() && role.asLong() != 0) language.put("role", role.asText());
		}
	}
}
