package me.whereareiam.yue;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class YueApplication {
	public static void main(String[] args) {
		SpringApplication.run(YueApplication.class, args);
	}

	@Bean
	@Qualifier("version")
	public String version() {
		return System.getProperty("app.version") != null ? System.getProperty("app.version") : "0.0.1-SNAPSHOT";
	}

	@Bean
	@Qualifier("dataPath")
	public Path dataPath() {
		return System.getProperty("app.dir") != null ? Paths.get(System.getProperty("app.dir")) : Paths.get(System.getProperty("userprofile.dir"));
	}

	@Bean
	@Qualifier("modulesPath")
	public Path modulesPath(@Qualifier("dataPath") Path dataPath) {
		Path modulesPath = dataPath.resolve("modules");

		if (!modulesPath.toFile().exists()) {
			boolean created = modulesPath.toFile().mkdirs();
			if (!created) throw new RuntimeException("Failed to create modules directory");
		}

		return modulesPath;
	}
}
