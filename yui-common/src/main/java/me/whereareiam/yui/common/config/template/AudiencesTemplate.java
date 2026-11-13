package me.whereareiam.yui.common.config.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.yui.model.config.audience.Audiences;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Yui's own audiences: {@code operator} manages the server, {@code moderator} is an operator or moderates members.
 */
@Component
@RequiredArgsConstructor
public class AudiencesTemplate implements DefaultsProvider<Audiences> {
	private final Configura configura;

	@Override
	public Audiences supply(Audiences audiences) {
		Map<String, Requirements> defaults = new LinkedHashMap<>();
		defaults.put("operator", requirements("- permission: MANAGE_SERVER"));
		defaults.put("moderator", requirements("""
				- any:
				    - audience: operator
				    - permission: MODERATE_MEMBERS
				"""));

		return new Audiences(defaults);
	}

	private Requirements requirements(String yaml) {
		try {
			return configura.mapper().readValue(yaml, Requirements.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Yui's own audiences cannot be read", exception);
		}
	}
}
