package me.whereareiam.yui.interests.model.message;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * {@code messages/<locale>/journey.yml}: the topics step of onboarding and the menu's placeholder.
 */
@Getter
@Setter
public class JourneyMessages {
	private Topics topics;

	@Getter
	@Setter
	public static class Topics {
		private String title;
		private List<String> description;
		private String placeholder;
	}
}
