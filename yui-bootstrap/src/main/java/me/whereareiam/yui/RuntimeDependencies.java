package me.whereareiam.yui;

import me.whereareiam.attache.model.Library;
import me.whereareiam.attache.platform.standalone.StandaloneLibraryManager;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.util.List;

/**
 * Loads runtime dependencies via Attache before beans that rely on them are created.
 */
@Configuration
public class RuntimeDependencies implements BeanFactoryPostProcessor, PriorityOrdered {
	private static final List<String> REPOSITORIES = List.of(
			"https://maven.whereareiam.me/release",
			"https://maven.whereareiam.me/development"
	);

	private static final List<Library> LIBRARIES = List.of(
			Library.builder()
					.groupId("net{}dv8tion")
					.artifactId("JDA")
					.version(BuildConfig.JDA)
					.resolveTransitiveDependencies(true)
					.build(),
			Library.builder()
					.groupId("org{}postgresql")
					.artifactId("postgresql")
					.version(BuildConfig.POSTGRES)
					.resolveTransitiveDependencies(true)
					.build(),
			Library.builder()
					.groupId("org{}incendo")
					.artifactId("cloud-core")
					.version(BuildConfig.CLOUD)
					.resolveTransitiveDependencies(true)
					.build(),
			Library.builder()
					.groupId("org{}incendo")
					.artifactId("cloud-annotations")
					.version(BuildConfig.CLOUD)
					.resolveTransitiveDependencies(true)
					.build(),
			Library.builder()
					.groupId("org{}incendo")
					.artifactId("cloud-processors-cooldown")
					.version(BuildConfig.CLOUD_COOLDOWN)
					.resolveTransitiveDependencies(true)
					.build(),
			Library.builder()
					.groupId("org{}incendo")
					.artifactId("cloud-jda6")
					.version(BuildConfig.CLOUD_DISCORD)
					.resolveTransitiveDependencies(true)
					.build(),
			Library.builder()
					.groupId("me{}whereareiam")
					.artifactId("configura")
					.version(BuildConfig.CONFIGURA)
					.resolveTransitiveDependencies(true)
					.build(),
			Library.builder()
					.groupId("me{}whereareiam")
					.artifactId("semantica")
					.version(BuildConfig.SEMANTICA)
					.resolveTransitiveDependencies(true)
					.build()
	);

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		StandaloneLibraryManager manager = beanFactory.getBeanProvider(StandaloneLibraryManager.class).getIfAvailable();
		if (manager == null)
			return;

		manager.addMavenCentral();
		registerDefaultRepositories(manager);
		loadLibraries(manager);
	}

	private void registerDefaultRepositories(StandaloneLibraryManager manager) {
		for (String repo : REPOSITORIES)
			manager.addRepository(repo);
	}

	private void loadLibraries(StandaloneLibraryManager manager) {
		for (Library library : RuntimeDependencies.LIBRARIES)
			manager.loadLibrary(library);
	}
}
