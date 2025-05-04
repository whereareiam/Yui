package me.whereareiam.yui.adapter.plugin.descriptor;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.api.exception.PluginLoadException;
import me.whereareiam.yui.api.model.plugin.Plugin;
import me.whereareiam.yui.api.output.config.ConfigurationLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@AllArgsConstructor
public class PluginDescriptorReader {
	private final ConfigurationLoader loader;

	public Plugin read(Path jar) throws Exception {
		try (FileSystem fs = FileSystems.newFileSystem(jar);
		     InputStream in = Files.newInputStream(fs.getPath("/plugin.json"))) {
			Plugin plugin = loader.load(in, Plugin.class);
			if (plugin.getId() == null || plugin.getName() == null || plugin.getEntrypoint() == null) {
				throw new PluginLoadException("Plugin missing required fields: id=" + plugin.getId() +
						", name=" + plugin.getName() + ", entrypoint=" + plugin.getEntrypoint());
			}
			return plugin;
		}
	}
}
