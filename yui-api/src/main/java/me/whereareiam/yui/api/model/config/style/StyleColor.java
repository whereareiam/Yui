package me.whereareiam.yui.api.model.config.style;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * A colour as Yui's style files and plugins name it: a palette name ({@code blue}) or a hex value
 * ({@code #E03131}). {@link Palette#resolve(StyleColor)} turns either into the colour Discord gets.
 *
 * <pre>{@code
 * StyleKit.embeds().info().setColor(StyleKit.palette().resolve(StyleColor.named("violet")));
 * StyleKit.embeds().info().setColor(StyleKit.palette().resolve(StyleColor.hex("#8A5CF6")));
 * }</pre>
 */
public sealed interface StyleColor {
	/**
	 * What a palette name is made of. It never starts with {@code #}, so a written colour is one or the other.
	 */
	Pattern NAME = Pattern.compile("[a-z][a-z0-9-]*");

	/**
	 * @param name a palette name, such as {@code blue}
	 * @return a reference to that palette colour
	 * @throws IllegalArgumentException when the name is not made of {@link #NAME}
	 */
	static @NotNull StyleColor named(@NotNull String name) {
		return new Named(name);
	}

	/**
	 * @param hex a colour in hex, such as {@code #8A5CF6}
	 * @return that colour
	 * @throws IllegalArgumentException when the text is no six-digit hex colour
	 */
	static @NotNull StyleColor hex(@NotNull String hex) {
		if (!hex.matches("#[0-9A-Fa-f]{6}"))
			throw new IllegalArgumentException("'" + hex + "' is no colour; write six hex digits after #, such as #8A5CF6");

		return new Hex(Integer.parseInt(hex.substring(1), 16));
	}

	/**
	 * Reads a colour as the files write it: a hex value when it starts with {@code #}, otherwise a palette name.
	 *
	 * @param text such as {@code blue} or {@code #E03131}
	 * @return the colour
	 */
	static @NotNull StyleColor parse(@NotNull String text) {
		return text.startsWith("#") ? hex(text) : named(text);
	}

	/**
	 * A colour of the palette, by its name.
	 */
	record Named(@NotNull String name) implements StyleColor {
		public Named {
			if (!NAME.matcher(name).matches())
				throw new IllegalArgumentException("'" + name + "' is no palette name; write lowercase letters, digits and -");
		}

		@Override
		public @NotNull String toString() {
			return name;
		}
	}

	/**
	 * A colour given directly, as its RGB value.
	 */
	record Hex(int rgb) implements StyleColor {
		@Override
		public @NotNull String toString() {
			return String.format("#%06X", rgb);
		}
	}
}
