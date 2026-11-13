package me.whereareiam.yui.util.style;

import me.whereareiam.yui.model.config.style.Palette;
import me.whereareiam.yui.model.config.style.StyleColor;
import me.whereareiam.yui.model.config.style.embed.Embed;
import me.whereareiam.yui.model.config.style.embed.EmbedStyle;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Embeds of each kind in {@code style/embeds.yml}, coloured through the palette. Each call returns a new builder.
 * A kind whose colour names no palette colour is refused when the embeds are built, so Yui does not start with it.
 */
public class Embeds {
	private final Palette palette;
	private final EmbedStyle embedStyle;

	public Embeds(Palette palette, EmbedStyle embedStyle) {
		this.palette = Objects.requireNonNull(palette, "palette must not be null");
		this.embedStyle = Objects.requireNonNull(embedStyle, "embedStyle must not be null");

		kinds().forEach(this::check);
	}

	public EmbedBuilder primary() {
		return build(embedStyle.getPrimary());
	}

	public EmbedBuilder secondary() {
		return build(embedStyle.getSecondary());
	}

	public EmbedBuilder success() {
		return build(embedStyle.getSuccess());
	}

	public EmbedBuilder warning() {
		return build(embedStyle.getWarning());
	}

	public EmbedBuilder error() {
		return build(embedStyle.getError());
	}

	public EmbedBuilder info() {
		return build(embedStyle.getInfo());
	}

	private EmbedBuilder build(Embed embed) {
		EmbedBuilder builder = new EmbedBuilder();
		if (embed == null) return builder;

		author(builder, embed.getAuthor());
		footer(builder, embed.getFooter());
		if (present(embed.getLink())) builder.setUrl(embed.getLink());
		if (present(embed.getThumbnail())) builder.setThumbnail(embed.getThumbnail());
		if (present(embed.getImage())) builder.setImage(embed.getImage());
		if (embed.isTimestamp()) builder.setTimestamp(Instant.now());
		if (embed.getColor() != null) builder.setColor(palette.resolve(embed.getColor()));

		return builder;
	}

	private Map<String, Embed> kinds() {
		Map<String, Embed> kinds = new LinkedHashMap<>();
		kinds.put("primary", embedStyle.getPrimary());
		kinds.put("secondary", embedStyle.getSecondary());
		kinds.put("success", embedStyle.getSuccess());
		kinds.put("warning", embedStyle.getWarning());
		kinds.put("error", embedStyle.getError());
		kinds.put("info", embedStyle.getInfo());

		return kinds;
	}

	private void check(String kind, Embed embed) {
		if (!(embed != null && embed.getColor() instanceof StyleColor.Named named)) return;

		if (palette.get(named.name()).isEmpty())
			throw new IllegalStateException("style/embeds.yml: " + kind + ".color names no colour '" + named.name()
					+ "' in style/palette.yml");
	}

	private static void author(EmbedBuilder builder, Embed.Author author) {
		if (author == null || !present(author.getName())) return;

		builder.setAuthor(author.getName(), author.getUrl(), author.getIcon());
	}

	private static void footer(EmbedBuilder builder, Embed.Footer footer) {
		if (footer == null || !present(footer.getText())) return;

		builder.setFooter(footer.getText(), footer.getIcon());
	}

	private static boolean present(String text) {
		return text != null && !text.isBlank();
	}
}
