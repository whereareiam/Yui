package me.whereareiam.yui.common.config;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.feature.polymorphic.PolymorphicFeature;
import me.whereareiam.configura.type.Format;
import me.whereareiam.yui.common.config.jackson.ConfigModule;
import me.whereareiam.yui.config.ConfigurationTypeResolver;
import me.whereareiam.yui.model.requirement.RequirementEntry;
import me.whereareiam.yui.model.requirement.type.ChannelTypeRequirement;
import me.whereareiam.yui.model.requirement.type.GuildRequirement;
import me.whereareiam.yui.model.requirement.type.RoleRequirement;
import me.whereareiam.yui.model.requirement.type.ScopeRequirement;
import me.whereareiam.yui.model.requirement.type.UserRequirement;
import me.whereareiam.yui.type.ConfigurationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Configura instance Yui reads its configuration files with: YAML or JSON as the installation chooses, Yui's
 * own value types, and the requirement entries told apart by the fields they hold.
 */
@Configuration
public class ConfiguraConfiguration {
	@Bean
	public Configura configura(ConfigurationTypeResolver resolver) {
		Format format = resolver.getConfigurationType() == ConfigurationType.JSON ? Format.JSON : Format.YAML;
		Configura configura = Configura.builder()
				.format(format)
				.module(new ConfigModule())
				.feature(requirements())
				.build();

		// The text loading still reaches Configura statically; it moves onto Semantica's own sources.
		Config.setConfigured(configura);

		return configura;
	}

	private static PolymorphicFeature requirements() {
		PolymorphicFeature feature = PolymorphicFeature.defaults();
		feature.register(RequirementEntry.class)
				.inferByField("roles", RoleRequirement.class)
				.inferByField("userIds", UserRequirement.class)
				.inferByField("guildIds", GuildRequirement.class)
				.inferByField("types", ChannelTypeRequirement.class)
				.inferByField("scopes", ScopeRequirement.class)
				.build();

		return feature;
	}
}
