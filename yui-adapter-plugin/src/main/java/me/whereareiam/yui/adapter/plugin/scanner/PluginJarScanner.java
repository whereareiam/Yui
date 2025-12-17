package me.whereareiam.yui.adapter.plugin.scanner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.plugin.descriptor.PluginDescriptorReader;
import me.whereareiam.yui.model.plugin.Plugin;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
@AllArgsConstructor
public class PluginJarScanner {
	private final PluginDescriptorReader descriptorReader;

	public Map<String, Plugin> scan(Path pluginsPath) {
		Map<String, Plugin> result = new LinkedHashMap<>();
		if (Files.notExists(pluginsPath))
			return result;

		try (Stream<Path> files = Files.list(pluginsPath)) {
			files.filter(p -> p.toString().endsWith(".jar"))
					.forEach(jar -> {
						try {
							Plugin plugin = descriptorReader.read(jar);
							String base = jar.getFileName().toString();
							base = base.endsWith(".jar") ? base.substring(0, base.length() - 4) : base;
							result.put(base, plugin);
						} catch (Exception e) {
							log.debug("[PluginScanner]: Skipping non-plugin jar {}: {}", jar, e.getMessage());
						}
					});
		} catch (IOException e) {
			log.debug("[PluginScanner]: Failed to scan plugins directory {}: {}", pluginsPath, e.getMessage());
		}
		return result;
	}
}


