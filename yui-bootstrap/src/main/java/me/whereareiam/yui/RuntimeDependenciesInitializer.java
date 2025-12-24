package me.whereareiam.yui;

import me.whereareiam.attache.platform.spring.config.AttacheProperties;
import me.whereareiam.attache.platform.spring.logging.SpringLoggingHelper;
import me.whereareiam.attache.platform.standalone.StandaloneLibraryManager;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Paths;

public class RuntimeDependenciesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		AttacheProperties properties = Binder.get(applicationContext.getEnvironment())
				.bind("attache", AttacheProperties.class)
				.orElseGet(AttacheProperties::new);

		if (!properties.isEnabled())
			return;

		if (applicationContext.getBeanFactory().containsBean("attacheLibraryManager"))
			return;

		StandaloneLibraryManager manager = new StandaloneLibraryManager(
				new SpringLoggingHelper(),
				Paths.get(properties.getLibraryPath()),
				"."
		);

		manager.setLogLevel(properties.getLogLevel());
		manager.setRepositoryResolutionMode(properties.getResolutionMode());
		manager.setVerbosityMode(properties.getVerbosityMode());

		properties.getRepositories().forEach(manager::addRepository);
		RuntimeDependencies.loadWith(manager, properties.isAddMavenCentral());

		applicationContext.getBeanFactory().registerSingleton("attacheLibraryManager", manager);
	}
}
