package me.whereareiam.yui.common.permission;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.Conditions;
import me.whereareiam.conditia.adapter.configura.ConfiguraSettings;
import me.whereareiam.conditia.common.ConditionsOptions;
import me.whereareiam.conditia.common.DefaultConditions;
import me.whereareiam.configura.Configura;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

/**
 * Yui's {@link Conditions}: every {@code requires:} is bound and checked through it. Core's condition types are
 * registered here, settings read with Yui's value types and enums in any case ({@code channel-type: text}), and
 * named lists are audiences ({@code audience: moderator}). Plugins register their types through their own scope.
 */
@Configuration
public class PermissionConfiguration {
	@Bean
	public Conditions conditions(Configura configura, List<ConditionType<?>> types) {
		Conditions conditions = new DefaultConditions(ConditionsOptions.builder()
				.reader(new ConfiguraSettings(settingsMapper(configura)))
				.namedLists(Optional.of("audience"))
				.build());
		types.forEach(conditions::register);

		return conditions;
	}

	@SuppressWarnings("deprecation")
	private static ObjectMapper settingsMapper(Configura configura) {
		ObjectMapper mapper = configura.mapper().copy();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);

		return mapper;
	}
}
