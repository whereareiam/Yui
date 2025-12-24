package me.whereareiam.yui.adapter.plugin.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.attache.common.BaseLibraryManager;
import me.whereareiam.attache.model.Library;
import me.whereareiam.yui.model.plugin.Plugin;
import me.whereareiam.yui.plugin.YuiPlugin;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuntimeDependencyLoader {
	private final BaseLibraryManager libraryManager;
	private final Set<String> loadedKeys = new LinkedHashSet<>();

	public void loadFromDescriptor(Plugin plugin) {
		Plugin.Runtime runtime = plugin.getRuntime();
		if (runtime == null)
			return;

		load(plugin.getId(), runtime.getRepositories(), runtime.getDependencies());
	}

	public void loadFromPlugin(Plugin descriptor, YuiPlugin plugin) {
		load(descriptor.getId(), plugin.repositories(), plugin.dependencies());
	}

	private void load(String pluginId, List<String> repositories, List<Library> dependencies) {
		if (dependencies == null || dependencies.isEmpty())
			return;

		registerRepositories(repositories);

		for (Library library : dependencies) {
			if (library == null) {
				log.warn("[PluginManager]: Skipping invalid runtime dependency for plugin '{}'", pluginId);
				continue;
			}

			String key = dependencyKey(library);
			if (loadedKeys.add(key)) {
				log.info("[PluginManager]: Loading runtime dependency {} for plugin '{}'", library, pluginId);
				libraryManager.loadLibrary(library);
				continue;
			}

			log.debug("[PluginManager]: Runtime dependency {} already loaded", library);
		}
	}

	private void registerRepositories(List<String> repositories) {
		if (repositories == null || repositories.isEmpty())
			return;

		for (String repo : repositories) {
			if (repo == null || repo.isBlank())
				continue;

			libraryManager.addRepository(repo);
		}
	}

	private String dependencyKey(Library library) {
		String classifier = library.getClassifier() == null ? "" : library.getClassifier();
		return library.getGroupId() + ":" + library.getArtifactId() + ":" + library.getVersion() + ":" + classifier;
	}
}
