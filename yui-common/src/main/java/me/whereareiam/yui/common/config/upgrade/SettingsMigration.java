package me.whereareiam.yui.common.config.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.strata.adapter.configura.ConfigContext;
import me.whereareiam.yui.model.type.Duration;

import java.util.List;

/**
 * {@code settings.yml}, migration 1: it takes the token, guild and intents out of {@code discord}, the updater says what
 * it checks and warns about as lists, and the database moves into {@code persistence.yml}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SettingsMigration {
	private static final String SETTINGS = "settings.yml";
	private static final String PERSISTENCE = "persistence.yml";

	static void apply(ConfigContext files) {
		if (!files.exists(SETTINGS)) return;

		files.move(SETTINGS, "/discord/token", SETTINGS, "/token");
		files.move(SETTINGS, "/discord/guildId", SETTINGS, "/guild");
		files.move(SETTINGS, "/discord/intents", SETTINGS, "/intents");

		updater(files.document(SETTINGS));
		JsonNode database = files.remove(SETTINGS, "/database");
		if (database instanceof ObjectNode old) persistence(old, files.document(PERSISTENCE));
	}

	private static void updater(ObjectNode settings) {
		if (!(settings.get("updater") instanceof ObjectNode updater) || !updater.has("checkForUpdates")) return;

		boolean checking = updater.path("checkForUpdates").asBoolean(true);
		ArrayNode check = updater.putArray("check");
		if (checking) check.add("yui");
		if (checking && updater.path("checkPluginUpdates").asBoolean(true)) check.add("plugins");

		ArrayNode warn = updater.putArray("warn");
		if (updater.path("warnAboutUpdates").asBoolean(true)) warn.add("updates");
		if (updater.path("warnAboutLocalBuilds").asBoolean(true)) warn.add("localBuilds");
		if (updater.path("warnAboutDevBuilds").asBoolean(true)) warn.add("devBuilds");

		updater.remove(List.of("checkForUpdates", "checkPluginUpdates", "warnAboutUpdates",
				"warnAboutLocalBuilds", "warnAboutDevBuilds"));
	}

	private static void persistence(ObjectNode database, ObjectNode persistence) {
		String type = database.path("type").asText("H2");
		persistence.put("type", type);

		if (type.equals("H2")) Nodes.copy(database, "file", persistence, "file");
		else server(database, persistence);

		if (database.get("hikari") instanceof ObjectNode hikari) persistence.set("pool", pool(hikari));
	}

	private static void server(ObjectNode database, ObjectNode persistence) {
		Nodes.copy(database, "hostname", persistence, "host");
		Nodes.copy(database, "port", persistence, "port");
		Nodes.copy(database, "database", persistence, "database");
		Nodes.copy(database, "username", persistence, "username");
		Nodes.copy(database, "password", persistence, "password");
	}

	private static ObjectNode pool(ObjectNode hikari) {
		ObjectNode pool = JsonNodeFactory.instance.objectNode();
		ObjectNode size = pool.putObject("size");
		Nodes.copy(hikari, "minimumIdle", size, "min");
		Nodes.copy(hikari, "maximumPoolSize", size, "max");

		ObjectNode timeouts = pool.putObject("timeouts");
		duration(hikari, "connectionTimeout", timeouts, "connect");
		duration(hikari, "idleTimeout", timeouts, "idle");
		duration(hikari, "maxLifetime", timeouts, "lifetime");

		return pool;
	}

	private static void duration(ObjectNode from, String key, ObjectNode to, String target) {
		JsonNode millis = from.get(key);
		if (millis != null && millis.canConvertToLong())
			to.put(target, Duration.ofSeconds(millis.asLong() / 1000).toString());
	}
}
