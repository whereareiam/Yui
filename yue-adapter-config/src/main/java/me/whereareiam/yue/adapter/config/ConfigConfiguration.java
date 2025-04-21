package me.whereareiam.yue.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.yue.adapter.config.factory.ConfigurationTypeFactory;
import me.whereareiam.yue.adapter.config.factory.ObjectMapperFactory;
import me.whereareiam.yue.adapter.config.provider.SettingsProvider;
import me.whereareiam.yue.adapter.config.provider.style.EmbedsProvider;
import me.whereareiam.yue.adapter.config.provider.style.PaletteProvider;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.model.config.style.Palette;
import me.whereareiam.yue.api.model.config.style.embed.EmbedStyle;
import me.whereareiam.yue.api.type.ConfigurationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigConfiguration {
	@Bean
	public ConfigurationType configurationType(ConfigurationTypeFactory provider) {
		ConfigurationType type = provider.getConfigurationType();
		if (type == null) throw new IllegalStateException("Configuration type is not set");

		return type;
	}

	@Bean
	public ObjectMapper objectMapper(ObjectMapperFactory factory) {
		return factory.createObjectMapper();
	}

	// Configs

	@Bean
	public Settings settings(SettingsProvider provider) {
		return provider.get();
	}

	@Bean
	public Palette palette(PaletteProvider provider) {
		return provider.get();
	}

	@Bean
	public EmbedStyle embeds(EmbedsProvider provider) {
		return provider.get();
	}
}
