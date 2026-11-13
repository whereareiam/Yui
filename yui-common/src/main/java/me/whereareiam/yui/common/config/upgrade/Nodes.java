package me.whereareiam.yui.common.config.upgrade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * What the migrations share about old values: one that is missing, null or blank was never set.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Nodes {
	static boolean present(JsonNode value) {
		return !value.isMissingNode() && !value.isNull() && !value.asText().isBlank();
	}

	static void copy(JsonNode from, String key, ObjectNode to, String target) {
		if (present(from.path(key))) to.set(target, from.get(key));
	}
}
