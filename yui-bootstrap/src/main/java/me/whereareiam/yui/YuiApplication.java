package me.whereareiam.yui;

import me.whereareiam.attache.platform.spring.config.AttacheProperties;
import me.whereareiam.attache.platform.spring.logging.SpringLoggingHelper;
import me.whereareiam.attache.platform.standalone.StandaloneLibraryManager;
import me.whereareiam.attache.type.VerbosityMode;
import me.whereareiam.yui.launcher.YuiLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class YuiApplication {
	public static void main(String[] args) {
		SpringApplication.run(YuiApplication.class, args);
	}

	@Bean("attacheLibraryManager")
	public StandaloneLibraryManager attacheLibraryManager(Environment environment) {
		AttacheProperties properties = Binder.get(environment)
				.bind("attache", AttacheProperties.class)
				.orElseGet(AttacheProperties::new);

		StandaloneLibraryManager manager = YuiLauncher.getLibraryManager();
		if (manager == null) {
			manager = new StandaloneLibraryManager(
					new SpringLoggingHelper(),
					Paths.get(properties.getLibraryPath()),
					".",
					YuiApplication.class.getClassLoader(),
					false
			);

			if (properties.isAddMavenCentral())
				manager.addMavenCentral();

			properties.getRepositories().forEach(manager::addRepository);

			if (properties.isEnabled())
				manager.loadClasspathDescriptors();
		}

		if (properties.isAddMavenCentral())
			manager.addMavenCentral();

		manager.setLogLevel(properties.getLogLevel());
		manager.setRepositoryResolutionMode(properties.getResolutionMode());
		manager.setVerbosityMode(resolveVerbosityMode(environment, properties));
		properties.getRepositories().forEach(manager::addRepository);

		YuiLauncher.setLibraryManager(manager);
		return manager;
	}

	private VerbosityMode resolveVerbosityMode(Environment environment, AttacheProperties properties) {
		return environment.getProperty("attache.verbosity-mode") == null
				? VerbosityMode.SUMMARY
				: properties.getVerbosityMode();
	}

	@Bean
	@Qualifier("version")
	public String version() {
		return System.getProperty("app.version") != null ? System.getProperty("app.version") : "0.0.1-SNAPSHOT";
	}

	@Bean
	@Qualifier("dataPath")
	public Path dataPath() {
		String dir = System.getProperty("app.dir");

		if (dir == null || dir.isEmpty())
			dir = Paths.get(".").toAbsolutePath().normalize().toString();

		Path dataPath = Paths.get(dir);

		if (!Files.exists(dataPath)) {
			try {
				Files.createDirectories(dataPath);
			} catch (Exception e) {
				throw new IllegalStateException("Failed to create data directory: " + dataPath, e);
			}
		}

		return dataPath;
	}

	@Bean
	@Qualifier("stylesPath")
	public Path stylesPath(@Qualifier("dataPath") Path dataPath) {
		Path stylesPath = dataPath.resolve(Constants.Structure.stylesDir);

		if (!stylesPath.toFile().exists()) {
			boolean created = stylesPath.toFile().mkdirs();
			if (!created) throw new RuntimeException("Failed to create styles directory");
		}

		return stylesPath;
	}

	@Bean
	@Qualifier("languagesPath")
	public Path languagesPath(@Qualifier("dataPath") Path dataPath) {
		Path languagesPath = dataPath.resolve(Constants.Structure.languagesDir);

		if (!languagesPath.toFile().exists()) {
			boolean created = languagesPath.toFile().mkdirs();
			if (!created) throw new RuntimeException("Failed to create languages directory");
		}

		return languagesPath;
	}
}
