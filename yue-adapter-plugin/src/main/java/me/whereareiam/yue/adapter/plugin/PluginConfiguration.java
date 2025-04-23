package me.whereareiam.yue.adapter.plugin;

import me.whereareiam.yue.shared.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class PluginConfiguration {
	@Bean
	@Qualifier("pluginsPath")
	public Path pluginsPath(@Qualifier("dataPath") Path dataPath) {
		Path plugins = dataPath.resolve(Constants.Structure.pluginsDir);

		if (!plugins.toFile().exists()) {
			boolean created = plugins.toFile().mkdirs();
			if (!created) throw new RuntimeException("Failed to create plugins directory");
		}

		return plugins;
	}
}
