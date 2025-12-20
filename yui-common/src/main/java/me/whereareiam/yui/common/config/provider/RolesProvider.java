package me.whereareiam.yui.common.config.provider;

import me.whereareiam.configura.Config;
import me.whereareiam.yui.common.config.template.RolesTemplate;
import me.whereareiam.yui.model.config.roles.Roles;
import org.springframework.stereotype.Component;

@Component
public class RolesProvider extends DefaultConfigProvider<Roles> {
	@Override
	protected Roles load() {
		return Config.update(getBasePath().resolve("roles"), Roles.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(RolesTemplate.class);
	}

	@Override
	public Class<Roles> getObjectType() {
		return Roles.class;
	}
}
