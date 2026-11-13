package me.whereareiam.yui.common.config.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.strata.adapter.configura.ConfigContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * {@code commands.yml}, migration 1: a command's {@code requirements} become a {@code requires:} list in Conditia's
 * form, and its cooldown a duration ({@code cooldown: 10} is {@code duration: 10s}). Roles by id, users, scopes and
 * channel types carry over; what cannot be said the new way, such as roles by name or an {@code XOR}, becomes
 * {@code audience: operator}, so a command never opens to more members than before.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CommandsMigration {
	private static final String COMMANDS = "commands.yml";
	private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

	// Each old group kind, told apart by the field it holds, as a condition; Yui serves one guild, so guilds limit nothing.
	private static final Map<String, Function<JsonNode, JsonNode>> GROUPS = new LinkedHashMap<>();

	static {
		GROUPS.put("roles", CommandsMigration::roles);
		GROUPS.put("userIds", group -> NODES.objectNode().set("user", texts(group.get("userIds"))));
		GROUPS.put("scopes", group -> scopes(group.get("scopes")));
		GROUPS.put("types", group -> NODES.objectNode().set("channel-type", kinds(group.get("types"))));
		GROUPS.put("guildIds", group -> NODES.missingNode());
	}

	static void apply(ConfigContext files) {
		if (!files.exists(COMMANDS) || !(files.document(COMMANDS).get("commands") instanceof ObjectNode commands)) return;

		for (Iterator<JsonNode> it = commands.elements(); it.hasNext(); )
			if (it.next() instanceof ObjectNode command) command(command);
	}

	private static void command(ObjectNode command) {
		JsonNode old = command.remove("requirements");
		if (old instanceof ObjectNode requirements && !command.has("requires")) command.set("requires", requires(requirements));

		if (command.get("cooldown") instanceof ObjectNode cooldown && cooldown.has("cooldown")) {
			cooldown.put("duration", cooldown.remove("cooldown").asLong() + "s");
			if (cooldown.path("group").asText("").isBlank()) cooldown.remove("group");
		}
	}

	private static ArrayNode requires(ObjectNode requirements) {
		List<JsonNode> conditions = new ArrayList<>();
		boolean safe = true;
		for (Iterator<JsonNode> groups = requirements.path("groups").elements(); groups.hasNext(); ) {
			JsonNode condition = condition(groups.next());
			if (condition == null) safe = false;
			else if (!condition.isMissingNode()) conditions.add(condition);
		}

		String operator = requirements.path("operator").asText("AND");
		ArrayNode requires = NODES.arrayNode();
		if (!safe || !List.of("AND", "OR", "NOT", "NOR").contains(operator)) return requires.add(operator());

		if (operator.equals("OR") && conditions.size() > 1) requires.addObject().set("any", NODES.arrayNode().addAll(conditions));
		else if (operator.equals("NOT") || operator.equals("NOR")) conditions.forEach(condition -> requires.addObject().set("not", condition));
		else requires.addAll(conditions);

		return requires;
	}

	// One old group as a condition; missing for one that limits nothing here, null for one that cannot be said.
	private static JsonNode condition(JsonNode group) {
		JsonNode condition = plain(group);
		if (condition == null || condition.isMissingNode()) return condition;

		return group.path("expected").asBoolean(true) ? condition : NODES.objectNode().set("not", condition);
	}

	private static JsonNode plain(JsonNode group) {
		return GROUPS.entrySet().stream()
				.filter(kind -> group.has(kind.getKey()))
				.findFirst()
				.map(kind -> kind.getValue().apply(group))
				.orElse(null);
	}

	private static JsonNode roles(JsonNode group) {
		if (!group.path("roleMatchBy").asText("ID").equalsIgnoreCase("ID")) return null;

		ArrayNode ids = texts(group.get("roles"));
		if (group.path("condition").asText("HAS").equals("HAS")) return NODES.objectNode().set("role", ids);

		ArrayNode any = NODES.arrayNode();
		ids.forEach(id -> any.addObject().set("role", id));
		return NODES.objectNode().set("any", any);
	}

	private static JsonNode scopes(JsonNode scopes) {
		ArrayNode any = NODES.arrayNode();
		for (JsonNode scope : scopes) {
			String where = scope.asText().toUpperCase(Locale.ROOT);
			any.addObject().put("scope", where.equals("GUILD") ? "guild" : "direct");
		}

		return any.size() == 1 ? any.get(0) : NODES.objectNode().set("any", any);
	}

	private static ArrayNode kinds(JsonNode types) {
		Map<String, String> kinds = Map.of(
				"TEXT", "text", "VOICE", "voice", "NEWS", "announcement", "STAGE", "stage", "FORUM", "forum",
				"PRIVATE", "direct", "GUILD_PUBLIC_THREAD", "thread", "GUILD_PRIVATE_THREAD", "thread", "GUILD_NEWS_THREAD", "thread"
		);
		ArrayNode list = NODES.arrayNode();
		for (JsonNode type : types) list.add(kinds.getOrDefault(type.asText().toUpperCase(Locale.ROOT), "other"));

		return list;
	}

	private static ArrayNode texts(JsonNode values) {
		ArrayNode list = NODES.arrayNode();
		if (values.isArray()) values.forEach(value -> list.add(value.asText()));
		else list.add(values.asText());

		return list;
	}

	private static ObjectNode operator() {
		return NODES.objectNode().put("audience", "operator");
	}
}
