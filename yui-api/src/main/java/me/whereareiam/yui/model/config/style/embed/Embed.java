package me.whereareiam.yui.model.config.style.embed;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.config.style.StyleColor;
import org.jetbrains.annotations.Nullable;

/**
 * How one kind of embed looks: its colour, and optionally an author, a footer, a title link, a thumbnail, an image
 * and a timestamp. Unset values are left out of the file.
 */
@Getter
@Setter
public class Embed {
	private @Nullable StyleColor color;
	private @Nullable Author author;
	private @Nullable Footer footer;
	private @Nullable String link;
	private @Nullable String thumbnail;
	private @Nullable String image;
	private boolean timestamp;

	/**
	 * The line above the title. Its {@code name} is a text and moves into {@code messages/<locale>/embeds.yml}.
	 */
	@Getter
	@Setter
	public static class Author {
		private @Nullable String name;
		private @Nullable String icon;
		private @Nullable String url;
	}

	/**
	 * The line below the embed. Its {@code text} moves into {@code messages/<locale>/embeds.yml}.
	 */
	@Getter
	@Setter
	public static class Footer {
		private @Nullable String text;
		private @Nullable String icon;
	}
}
