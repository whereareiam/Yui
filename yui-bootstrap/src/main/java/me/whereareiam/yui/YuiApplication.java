package me.whereareiam.yui;

import me.whereareiam.yui.shared.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Path;
import java.nio.file.Paths;

@EnableAsync
@SpringBootApplication
public class YuiApplication {
	public static void main(String[] args) {
		SpringApplication.run(YuiApplication.class, args);
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
