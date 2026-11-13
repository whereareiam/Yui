package me.whereareiam.yui.api.model.config.style.embed;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code style/embeds.yml}: how each kind of embed looks. Code asks for a kind ({@code StyleKit.embeds().error()})
 * and gets an embed in its colour and decoration.
 *
 * <pre>{@code
 * primary:
 *   color: brand
 *   timestamp: true
 * error:
 *   color: "#E03131"
 * }</pre>
 */
@Getter
@Setter
public class EmbedStyle {
	private Embed primary;
	private Embed secondary;
	private Embed success;
	private Embed warning;
	private Embed error;
	private Embed info;
}
