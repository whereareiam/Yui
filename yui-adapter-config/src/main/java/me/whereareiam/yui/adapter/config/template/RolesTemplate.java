package me.whereareiam.yui.adapter.config.template;

import me.whereareiam.yui.api.model.config.Roles;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RolesTemplate implements DefaultConfig<Roles> {
	@Override
	public Roles getDefault() {
		Roles roles = new Roles();

		// Default values
		roles.setLanguageRoles(Map.of());
		roles.setAllowedRoles(Map.of());

		return roles;
	}
}
