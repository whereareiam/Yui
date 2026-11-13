package me.whereareiam.yui.common.config.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.strata.adapter.configura.ConfigContext;

import java.util.List;

/**
 * {@code style/palette.yml} and {@code style/embeds.yml}, migration 1: the style moves from {@code styles/} to
 * {@code style/}, the palette names its colours and each embed kind refers to one; empty values are left out.
 *
 * <p>The old palette was keyed by embed kind, so its colours keep those names ({@code error: "#FF4C4C"}) and each
 * kind refers to its own ({@code error.color: error}): nobody's colours change.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class StyleMigration {
	static final String OLD_PALETTE = "styles/palette.yml";
	static final String OLD_EMBEDS = "styles/embeds.yml";

	private static final String PALETTE = "style/palette.yml";
	private static final String EMBEDS = "style/embeds.yml";
	private static final List<String> KINDS = List.of("primary", "secondary", "success", "warning", "error", "info");

	static void palette(ConfigContext files) {
		if (!files.exists(OLD_PALETTE)) return;

		ObjectNode old = files.document(OLD_PALETTE);
		ObjectNode palette = files.document(PALETTE);
		for (String kind : KINDS)
			if (Nodes.present(old.path(kind))) palette.set(kind, old.get(kind));

		files.delete(OLD_PALETTE);
		files.deleteDirectoryIfEmpty("styles");
	}

	static void embeds(ConfigContext files) {
		if (!files.exists(OLD_EMBEDS)) return;

		ObjectNode old = files.document(OLD_EMBEDS);
		ObjectNode embeds = files.document(EMBEDS);
		JsonNode palette = files.exists(PALETTE) ? files.document(PALETTE) : JsonNodeFactory.instance.objectNode();
		for (String kind : KINDS) {
			ObjectNode embed = embed(old.path(kind));
			if (palette.has(kind)) embed.put("color", kind);
			if (!embed.isEmpty()) embeds.set(kind, embed);
		}

		files.delete(OLD_EMBEDS);
		files.deleteDirectoryIfEmpty("styles");
	}

	private static ObjectNode embed(JsonNode old) {
		ObjectNode embed = JsonNodeFactory.instance.objectNode();
		ObjectNode author = JsonNodeFactory.instance.objectNode();
		Nodes.copy(old.path("author"), "name", author, "name");
		Nodes.copy(old.path("author"), "iconUrl", author, "icon");
		Nodes.copy(old.path("author"), "url", author, "url");
		if (!author.isEmpty()) embed.set("author", author);

		ObjectNode footer = JsonNodeFactory.instance.objectNode();
		Nodes.copy(old.path("footer"), "text", footer, "text");
		Nodes.copy(old.path("footer"), "iconUrl", footer, "icon");
		if (!footer.isEmpty()) embed.set("footer", footer);

		Nodes.copy(old, "titleUrl", embed, "link");
		Nodes.copy(old, "thumbnailUrl", embed, "thumbnail");
		Nodes.copy(old, "imageUrl", embed, "image");
		if (old.path("timestamp").asBoolean(false)) embed.put("timestamp", true);

		return embed;
	}
}
