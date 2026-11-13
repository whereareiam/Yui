package me.whereareiam.yui.interests.model.message;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code messages/<locale>/topics.yml}: each topic's name and description, by the id {@code topics.yml} gives it.
 * Without a name the menu shows the id.
 *
 * <pre>{@code
 * development:
 *   name: "Development"
 *   description: "Code, tools and the dev channel"
 * }</pre>
 */
public class TopicMessages {
	private final Map<String, Topic> topics = new LinkedHashMap<>();

	@JsonAnySetter
	void put(String id, Topic topic) {
		topics.put(id, topic);
	}

	@JsonAnyGetter
	public Map<String, Topic> all() {
		return topics;
	}

	@Getter
	@Setter
	public static class Topic {
		private String name;
		private String description;
	}
}
