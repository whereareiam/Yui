package me.whereareiam.yui.api.model.config.style;

import me.whereareiam.configura.annotation.PreserveUnknownFields;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@code style/palette.yml}: Yui's named colours. Embed kinds, plugins' embeds and later components refer to them by
 * name, so a colour is changed in one place.
 *
 * <pre>{@code
 * brand: "#B57E3F"
 * red: "#FF4C4C"
 * }</pre>
 */
@PreserveUnknownFields
public class Palette {
	private final Map<String, Color> colors;

	public Palette() {
		this(Map.of());
	}

	/**
	 * @param colors the colours by name
	 */
	public Palette(@NotNull Map<String, Color> colors) {
		this.colors = Collections.unmodifiableMap(new LinkedHashMap<>(colors));
	}

	/**
	 * @return every colour by name, in the file's order
	 */
	public @NotNull Map<String, Color> all() {
		return colors;
	}

	/**
	 * @param name a palette name, such as {@code blue}
	 * @return its colour, if the palette has one by that name
	 */
	public @NotNull Optional<Color> get(@NotNull String name) {
		return Optional.ofNullable(colors.get(name));
	}

	/**
	 * Turns a style colour into the colour Discord gets.
	 *
	 * @param color a palette name or a hex value
	 * @return the colour
	 * @throws IllegalArgumentException when a palette name is not in the palette
	 */
	public @NotNull Color resolve(@NotNull StyleColor color) {
		return switch (color) {
			case StyleColor.Hex hex -> new Color(hex.rgb());
			case StyleColor.Named named -> get(named.name()).orElseThrow(() ->
					new IllegalArgumentException("No colour '" + named.name() + "' in style/palette.yml"));
		};
	}
}
