package me.whereareiam.yui.common.config.template;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.roles.RoleEntry;
import me.whereareiam.yui.model.config.roles.Roles;
import me.whereareiam.yui.model.config.roles.SyncSettings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class RolesTemplate implements TemplateProvider<Roles> {
	@Override
	public Roles supply(Roles config) {
		config.setRoles(new ArrayList<>());

		if (config.getRoles().isEmpty()) {
			RoleEntry exampleRole = new RoleEntry();
			exampleRole.setId(0L);
			exampleRole.setSync(true);
			exampleRole.setName("Example Role");
			exampleRole.setDescription("This is an example role. Replace the ID with your actual role ID.");
			config.getRoles().add(exampleRole);
		}

		SyncSettings syncSettings = new SyncSettings();
		config.setSync(syncSettings);

		return config;
	}
}
