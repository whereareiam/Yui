package me.whereareiam.yui.common.config.template;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.Roles;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RolesTemplate implements TemplateProvider<Roles> {
	@Override
	public Roles supply(Roles roles) {
		// Default values
		roles.setLanguageRoles(Map.of());
		roles.setAllowedRoles(Map.of());

		return roles;
	}
}
