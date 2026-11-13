package me.whereareiam.yui.interests.model.message;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * {@code messages/<locale>/commands.yml}: the texts of {@code /topics}.
 */
@Getter
@Setter
public class CommandMessages {
	private Topics topics;

	@Getter
	@Setter
	public static class Topics {
		private String description;
		private String example;
		private Reply menu;
		private Reply saved;
	}

	@Getter
	@Setter
	public static class Reply {
		private String title;
		private List<String> description;
	}
}
