package me.whereareiam.yue.adapter.config.provider;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yue.adapter.config.management.ConfigLoader;
import me.whereareiam.yue.api.input.Registry;
import me.whereareiam.yue.api.model.config.Roles;
import me.whereareiam.yue.api.output.Reloadable;
import me.whereareiam.yue.api.output.provider.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class RolesProvider implements Provider<Roles>, Reloadable {
	private final Path dataPath;
	private final ConfigLoader configLoader;

	private Roles roles;

	@Autowired
	public RolesProvider(@Qualifier("dataPath") Path dataPath,
	                     ConfigLoader configLoader,
	                     Registry<Reloadable> registry) {
		this.dataPath = dataPath;
		this.configLoader = configLoader;

		registry.register(this);
	}

	@PostConstruct
	public void init() {
		load();
	}

	@Override
	public Roles get() {
		if (roles == null) {
			load();
		}
		return roles;
	}

	@Override
	public void reload() {
		load();
	}

	private void load() {
		roles = configLoader.load(dataPath.resolve("roles"), Roles.class);
	}
}