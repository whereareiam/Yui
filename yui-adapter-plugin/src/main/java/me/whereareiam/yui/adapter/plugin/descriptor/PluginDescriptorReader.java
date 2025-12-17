package me.whereareiam.yui.adapter.plugin.descriptor;

import me.whereareiam.configura.Config;
import me.whereareiam.yui.exception.PluginLoadException;
import me.whereareiam.yui.model.plugin.Plugin;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PluginDescriptorReader {
	public Plugin read(Path jar) throws Exception {
		try (FileSystem fs = FileSystems.newFileSystem(jar);
		     InputStream in = Files.newInputStream(fs.getPath("/plugin.json"))) {
			Plugin plugin = Config.load(in, Plugin.class);
			if (plugin.getId() == null || plugin.getName() == null || plugin.getEntrypoint() == null) {
				throw new PluginLoadException("Plugin missing required fields: id=" + plugin.getId() +
						", name=" + plugin.getName() + ", entrypoint=" + plugin.getEntrypoint());
			}
			return plugin;
		}
	}
}
