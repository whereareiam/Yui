package me.whereareiam.yui.common.config.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.strata.adapter.configura.ConfigContext;

/**
 * {@code settings.yml}, migration 3: the categories of temporary conversation channels move into
 * {@code conversations.yml}, and the {@code discord} block, now empty, goes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ConversationsMigration {
	private static final String SETTINGS = "settings.yml";
	private static final String CONVERSATIONS = "conversations.yml";

	static void apply(ConfigContext files) {
		if (!files.exists(SETTINGS)) return;

		JsonNode categories = files.remove(SETTINGS, "/discord/channels/tempChannelCategories");
		files.removeIfEmpty(SETTINGS, "/discord/channels");
		files.removeIfEmpty(SETTINGS, "/discord");
		if (categories == null || categories.isMissingNode() || categories.isEmpty()) return;

		files.document(CONVERSATIONS).putObject("types").putObject("channel").set("categories", categories);
	}
}
