package me.whereareiam.yui.interests.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code plugins/interests/topics.yml}: the topics members may pick, by id, in the order they are offered.
 *
 * <pre>{@code
 * development:
 *   role: "123456789012345680"
 * minecraft:
 *   role: "123456789012345681"
 * }</pre>
 */
public class Topics {
	private final Map<String, Topic> topics;

	public Topics() {
		this(Map.of());
	}

	public Topics(@NotNull Map<String, Topic> topics) {
		this.topics = Collections.unmodifiableMap(new LinkedHashMap<>(topics));
	}

	/**
	 * @return every topic by id, in the file's order
	 */
	public @NotNull Map<String, Topic> all() {
		return topics;
	}
}
