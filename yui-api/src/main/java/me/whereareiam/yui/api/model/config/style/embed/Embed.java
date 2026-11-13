package me.whereareiam.yui.api.model.config.style.embed;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.model.config.style.StyleColor;
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
	 * The line above the title: its icon and link. Its name is the text {@code embeds.<kind>.author}.
	 */
	@Getter
	@Setter
	public static class Author {
		private @Nullable String icon;
		private @Nullable String url;
	}

	/**
	 * The line below the embed: its icon. Its text is the text {@code embeds.<kind>.footer}.
	 */
	@Getter
	@Setter
	public static class Footer {
		private @Nullable String icon;
	}
}
