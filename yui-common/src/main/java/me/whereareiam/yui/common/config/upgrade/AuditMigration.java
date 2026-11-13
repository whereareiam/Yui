package me.whereareiam.yui.common.config.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.strata.adapter.configura.ConfigContext;

import java.util.List;
import java.util.Map;

/**
 * {@code settings.yml}, migration 2: the audit channels move into {@code audit.yml}, one per category. A category
 * takes the channel of its first old type that had one ({@code members} that of {@code user_join}, else of
 * {@code user_leave}); when members were posted but leaving members were not, {@code member.leave} is switched off.
 * Channels per locale ({@code ru:123}) are no more: the first channel without a locale wins, or else the first.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class AuditMigration {
	private static final String SETTINGS = "settings.yml";
	private static final String AUDIT = "audit.yml";
	private static final Map<String, String> CATEGORIES = Map.of(
			"user_join", "members",
			"user_leave", "members",
			"user_kick", "moderation",
			"update_*_available", "system"
	);
	private static final Map<String, String> ENTRIES = Map.of(
			"user_join", "member.join",
			"user_leave", "member.leave",
			"user_kick", "member.kick"
	);

	static void apply(ConfigContext files) {
		if (!files.exists(SETTINGS)) return;

		JsonNode old = files.remove(SETTINGS, "/discord/channels/audit");
		files.removeIfEmpty(SETTINGS, "/discord/channels");
		if (!(old instanceof ObjectNode types)) return;

		ObjectNode audit = files.document(AUDIT);
		ObjectNode categories = audit.putObject("categories");
		for (String type : new String[]{"user_join", "user_kick", "update_*_available", "user_leave"}) {
			String channel = channel(types.path(type));
			if (channel != null && !categories.has(CATEGORIES.get(type)))
				categories.putObject(CATEGORIES.get(type)).put("channel", channel);
		}

		if (categories.has("members") && channel(types.path("user_leave")) == null)
			audit.putObject("entries").put(ENTRIES.get("user_leave"), "off");
		if (categories.isEmpty()) audit.remove("categories");
	}

	private static String channel(JsonNode value) {
		String first = null;
		for (JsonNode item : value.isArray() ? value : List.of(value)) {
			String text = item.asText("");
			if (text.isBlank()) continue;

			int colon = text.indexOf(':');
			if (colon < 0) return text;
			if (first == null) first = text.substring(colon + 1);
		}

		return first;
	}
}
