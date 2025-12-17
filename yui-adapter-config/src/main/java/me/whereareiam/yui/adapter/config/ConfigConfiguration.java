package me.whereareiam.yui.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.yui.adapter.config.factory.ConfigurationTypeFactory;
import me.whereareiam.yui.adapter.config.factory.ObjectMapperFactory;
import me.whereareiam.yui.adapter.config.provider.RolesProvider;
import me.whereareiam.yui.adapter.config.provider.SettingsProvider;
import me.whereareiam.yui.adapter.config.provider.style.EmbedsProvider;
import me.whereareiam.yui.adapter.config.provider.style.PaletteProvider;
import me.whereareiam.yui.model.config.Roles;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.config.style.Palette;
import me.whereareiam.yui.model.config.style.embed.EmbedStyle;
import me.whereareiam.yui.type.ConfigurationType;
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
	public Roles roles(RolesProvider provider) {
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
