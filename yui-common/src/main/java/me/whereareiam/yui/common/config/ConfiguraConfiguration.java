package me.whereareiam.yui.common.config;

import me.whereareiam.conditia.adapter.configura.ConditiaFeature;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.yui.common.config.jackson.ConfigModule;
import me.whereareiam.yui.config.ConfigurationTypeResolver;
import me.whereareiam.yui.type.ConfigurationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Configura instance Yui reads its configuration files with: YAML or JSON as the installation chooses, Yui's
 * own value types and Conditia's {@code requires:} lists.
 */
@Configuration
public class ConfiguraConfiguration {
	@Bean
	public Configura configura(ConfigurationTypeResolver resolver) {
		return build(resolver.getConfigurationType());
	}

	/**
	 * Builds the instance for files in the given format.
	 *
	 * @param type YAML or JSON
	 * @return a new instance
	 */
	public static Configura build(ConfigurationType type) {
		return Configura.builder()
				.format(type == ConfigurationType.JSON ? Format.JSON : Format.YAML)
				.module(new ConfigModule())
				.feature(new ConditiaFeature())
				.build();
	}
}
