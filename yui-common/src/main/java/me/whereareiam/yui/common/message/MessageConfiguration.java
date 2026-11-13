package me.whereareiam.yui.common.message;

import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.semantica.catalog.Layer;
import me.whereareiam.semantica.common.Semantica;
import me.whereareiam.semantica.source.file.FileSource;
import me.whereareiam.semantica.syntax.icu.IcuSyntax;
import me.whereareiam.yui.api.message.MessageDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.List;

/**
 * Yui's texts, through Semantica: the English defaults of every {@link MessageDefaults} first, then
 * {@code messages/<locale>/} over them, read only and watched. Every key exists in English, so English is the last
 * locale a lookup tries.
 */
@Configuration
public class MessageConfiguration {
	/**
	 * Reads text files as written: unlike configuration, a text names no environment variables.
	 */
	static final Configura TEXTS = Configura.builder().format(Format.YAML).build();

	@Bean(destroyMethod = "close")
	public Semantica semantica(List<MessageDefaults<?>> defaults, @Qualifier("messagesPath") Path messages) {
		return Semantica.builder()
				.syntax(new IcuSyntax())
				.defaultLocale(DefaultsSource.ENGLISH)
				.layer(Layer.of(new DefaultsSource("yui:defaults", "", defaults, TEXTS.mapper()), Layer.DEFAULTS))
				.layer(Layer.of(FileSource.of(messages, TEXTS), Layer.OVERRIDES))
				.build();
	}
}
