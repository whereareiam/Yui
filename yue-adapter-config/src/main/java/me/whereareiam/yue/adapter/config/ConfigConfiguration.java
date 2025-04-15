package me.whereareiam.yue.adapter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.yue.adapter.config.factory.ConfigurationTypeFactory;
import me.whereareiam.yue.adapter.config.factory.ObjectMapperFactory;
import me.whereareiam.yue.adapter.config.provider.CommandsProvider;
import me.whereareiam.yue.adapter.config.provider.SettingsProvider;
import me.whereareiam.yue.api.model.config.Commands;
import me.whereareiam.yue.api.model.config.settings.Settings;
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
	public Commands commands(CommandsProvider provider) {
		return provider.get();
	}
}
