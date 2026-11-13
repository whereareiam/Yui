package me.whereareiam.yui.model.config.messages;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code messages/<locale>/embeds.yml}: the author and footer texts of each embed kind. A kind without them shows
 * no author or footer.
 */
@Getter
@Setter
public class EmbedMessages {
	private Kind primary;
	private Kind secondary;
	private Kind success;
	private Kind warning;
	private Kind error;
	private Kind info;

	@Getter
	@Setter
	public static class Kind {
		private String author;
		private String footer;
	}
}
