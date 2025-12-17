package me.whereareiam.yui.common.config.provider;

import me.whereareiam.configura.Config;
import me.whereareiam.yui.common.config.template.RolesTemplate;
import me.whereareiam.yui.model.config.Roles;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.Reloadable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class RolesProvider extends DefaultConfigProvider<Roles> {
	@Autowired
	public RolesProvider(
			@Qualifier("dataPath") Path dataPath,
			Registry<Reloadable> registry
	) {
		super(dataPath, registry);
	}

	@Override
	protected Roles load() {
		return Config.update(getBasePath().resolve("roles"), Roles.class);
	}

	@Override
	protected void registerTemplate() {
		Config.registerTemplate(RolesTemplate.class);
	}
}