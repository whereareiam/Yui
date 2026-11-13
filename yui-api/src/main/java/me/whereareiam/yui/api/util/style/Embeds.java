package me.whereareiam.yui.api.util.style;

import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.config.style.Palette;
import me.whereareiam.yui.api.model.config.style.StyleColor;
import me.whereareiam.yui.api.model.config.style.embed.Embed;
import me.whereareiam.yui.api.model.config.style.embed.EmbedStyle;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Embeds of each kind in {@code style/embeds.yml}, coloured through the palette, with the author and footer texts
 * of {@code messages/<locale>/embeds.yml}. Each call returns a new builder.
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
		return build("primary", embedStyle.getPrimary());
	}

	public EmbedBuilder secondary() {
		return build("secondary", embedStyle.getSecondary());
	}

	public EmbedBuilder success() {
		return build("success", embedStyle.getSuccess());
	}

	public EmbedBuilder warning() {
		return build("warning", embedStyle.getWarning());
	}

	public EmbedBuilder error() {
		return build("error", embedStyle.getError());
	}

	public EmbedBuilder info() {
		return build("info", embedStyle.getInfo());
	}

	private EmbedBuilder build(String kind, Embed embed) {
		EmbedBuilder builder = new EmbedBuilder();
		if (embed == null) return builder;

		author(builder, text(kind, "author"), embed.getAuthor());
		footer(builder, text(kind, "footer"), embed.getFooter());
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

	private static void author(EmbedBuilder builder, String name, Embed.Author author) {
		if (!present(name)) return;

		builder.setAuthor(name, author == null ? null : author.getUrl(), author == null ? null : author.getIcon());
	}

	private static void footer(EmbedBuilder builder, String text, Embed.Footer footer) {
		if (!present(text)) return;

		builder.setFooter(text, footer == null ? null : footer.getIcon());
	}

	// An embed's author and footer are texts in Yui's default locale; a kind without one shows none.
	private static String text(String kind, String part) {
		String key = "embeds." + kind + "." + part;
		String text = Translatable.text(key).resolveDefault();

		return text.equals(key) ? null : text;
	}

	private static boolean present(String text) {
		return text != null && !text.isBlank();
	}
}
