package me.whereareiam.yui.common.config;

import me.whereareiam.conditia.adapter.configura.ConditiaFeature;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.strata.adapter.configura.StrataFeature;
import me.whereareiam.yui.common.config.jackson.ConfigModule;
import me.whereareiam.yui.common.config.upgrade.ConfigUpgrade;
import me.whereareiam.yui.config.ConfigurationTypeResolver;
import me.whereareiam.yui.type.ConfigurationType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.List;

/**
 * The Configura instance Yui reads its configuration files with: YAML or JSON as the installation chooses, Yui's
 * own value types, Conditia's {@code requires:} lists, and the {@code _version} Strata keeps in a file left out of
 * the models.
 */
@Configuration
public class ConfiguraConfiguration {
	/**
	 * Yui's instance, once the files of the data folder are known to need no upgrade.
	 *
	 * @throws IllegalStateException when the files are older than this version reads; {@code yui config upgrade}
	 *                               brings them up to date
	 */
	@Bean
	public Configura configura(ConfigurationTypeResolver resolver, @Qualifier("dataPath") Path dataPath) {
		Configura configura = build(resolver.getConfigurationType());

		List<String> pending = new ConfigUpgrade(configura, dataPath).pending();
		if (!pending.isEmpty())
			throw new IllegalStateException("The configuration in " + dataPath + " needs an upgrade (" + String.join(", ", pending)
					+ "); run: yui config upgrade " + dataPath);

		// The text loading still reaches Configura statically; it moves onto Semantica's own sources.
		Config.setConfigured(configura);

		return configura;
	}

	/**
	 * Builds the instance for files in the given format, for Yui and for {@code yui config upgrade}.
	 *
	 * @param type YAML or JSON
	 * @return a new instance
	 */
	public static Configura build(ConfigurationType type) {
		return Configura.builder()
				.format(type == ConfigurationType.JSON ? Format.JSON : Format.YAML)
				.module(new ConfigModule())
				.feature(new StrataFeature())
				.feature(new ConditiaFeature())
				.build();
	}
}
