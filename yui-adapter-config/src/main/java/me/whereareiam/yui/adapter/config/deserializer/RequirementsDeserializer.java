package me.whereareiam.yui.adapter.config.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import me.whereareiam.yui.model.requirement.*;
import me.whereareiam.yui.model.requirement.type.*;
import me.whereareiam.yui.type.requirement.RequirementOperator;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom deserializer for Requirements that handles polymorphic deserialization
 * of different requirement types based on the map key.
 */
public class RequirementsDeserializer extends JsonDeserializer<Requirements> {
	@Override
	public Requirements deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		ObjectCodec codec = p.getCodec();
		JsonNode node = codec.readTree(p);

		Requirements requirements = new Requirements();

		if (node.has("operator"))
			requirements.setOperator(RequirementOperator.valueOf(node.get("operator").asText()));

		if (node.has("groups")) {
			JsonNode groupsNode = node.get("groups");
			Map<String, RequirementEntry> groups = new LinkedHashMap<>();

			groupsNode.fields().forEachRemaining(entry -> {
				String key = entry.getKey();
				JsonNode value = entry.getValue();
				try {
					groups.put(key, createRequirement(codec, value, key));
				} catch (IOException e) {
					throw new RuntimeException("Failed to deserialize requirement for key: " + key, e);
				}
			});

			requirements.setGroups(groups);
		}

		return requirements;
	}

	private RequirementEntry createRequirement(ObjectCodec codec, JsonNode node, String key) throws IOException {
		// Use the map key to determine the requirement type
		return switch (key.toLowerCase()) {
			case "user" -> codec.treeToValue(node, UserRequirement.class);
			case "role" -> codec.treeToValue(node, RoleRequirement.class);
			case "guild" -> codec.treeToValue(node, GuildRequirement.class);
			case "scope" -> codec.treeToValue(node, ScopeRequirement.class);
			case "channel" -> codec.treeToValue(node, ChannelTypeRequirement.class);
			default -> throw new IllegalArgumentException(
				"Unknown requirement key: '" + key + "'. " +
				"Supported keys: user, role, guild, scope, channel"
			);
		};
	}
}
