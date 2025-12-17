package me.whereareiam.yui.style;

import me.whereareiam.yui.model.config.style.Palette;
import me.whereareiam.yui.model.config.style.embed.Embed;
import me.whereareiam.yui.model.config.style.embed.EmbedStyle;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.Instant;
import java.util.Objects;

public class Embeds {
	private final Palette palette;
	private final EmbedStyle embedStyle;

	public Embeds(Palette palette, EmbedStyle embedStyle) {
		this.palette = Objects.requireNonNull(palette, "palette must not be null");
		this.embedStyle = Objects.requireNonNull(embedStyle, "embedStyle must not be null");
	}

	public EmbedBuilder primary() {
		return build(embedStyle.getPrimary(), palette.getPrimary());
	}

	public EmbedBuilder secondary() {
		return build(embedStyle.getSecondary(), palette.getSecondary());
	}

	public EmbedBuilder success() {
		return build(embedStyle.getSuccess(), palette.getSuccess());
	}

	public EmbedBuilder warning() {
		return build(embedStyle.getWarning(), palette.getWarning());
	}

	public EmbedBuilder error() {
		return build(embedStyle.getError(), palette.getError());
	}

	public EmbedBuilder info() {
		return build(embedStyle.getInfo(), palette.getInfo());
	}

	private EmbedBuilder build(Embed embed, Color color) {
		EmbedBuilder builder = new EmbedBuilder();

		if (embed == null)
			return builder;

		setAuthorIfPresent(builder, embed.getAuthor());
		setFooterIfPresent(builder, embed.getFooter());
		setUrlIfPresent(builder, embed.getTitleUrl());
		setThumbnailIfPresent(builder, embed.getThumbnailUrl());
		setImageIfPresent(builder, embed.getImageUrl());

		if (embed.isTimestamp())
			builder.setTimestamp(Instant.now());

		builder.setColor(color);

		return builder;
	}

	private void setAuthorIfPresent(EmbedBuilder b, Embed.Author a) {
		if (a == null || isBlank(a.getName())) return;

		b.setAuthor(
				a.getName(),
				blankToNull(a.getUrl()),
				blankToNull(a.getIconUrl())
		);
	}

	private void setFooterIfPresent(EmbedBuilder b, Embed.Footer f) {
		if (f == null || isBlank(f.getText())) return;
		b.setFooter(
				f.getText(),
				blankToNull(f.getIconUrl())
		);
	}

	private void setUrlIfPresent(EmbedBuilder b, String url) {
		if (!isBlank(url))
			b.setUrl(url);
	}

	private void setThumbnailIfPresent(EmbedBuilder b, String url) {
		if (!isBlank(url))
			b.setThumbnail(url);
	}

	private void setImageIfPresent(EmbedBuilder b, String url) {
		if (!isBlank(url))
			b.setImage(url);
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	private static String blankToNull(String s) {
		return isBlank(s) ? null : s;
	}
}