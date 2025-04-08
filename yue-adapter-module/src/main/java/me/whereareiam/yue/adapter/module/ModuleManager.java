package me.whereareiam.yue.adapter.module;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yue.api.model.module.InternalModule;
import me.whereareiam.yue.api.model.module.Module;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import me.whereareiam.yue.api.output.module.ModuleService;
import me.whereareiam.yue.api.type.ModuleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

@Getter
@Setter
@Service
public class ModuleManager implements ModuleService {
	private static final String MODULE_FILE = "module.json";

	private final Path modulesPath;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ConfigurationLoader configurationLoader;
	private final ModuleLifecycleController lifecycleController;

	private List<InternalModule> modules = new ArrayList<>();

	@Autowired
	public ModuleManager(@Qualifier("modulesPath") Path modulesPath, ConfigurationLoader configurationLoader,
	                     ModuleLifecycleController moduleLifecycleController) {
		this.modulesPath = modulesPath;
		this.configurationLoader = configurationLoader;
		this.lifecycleController = moduleLifecycleController;
	}

	@Override
	public Optional<InternalModule> getModule(String name) {
		return modules.stream().filter(module -> module.getName().equals(name)).findFirst();
	}

	@Override
	public void loadModules() {
		discoverModules();
		modules.forEach(lifecycleController::loadModule);
		modules.forEach(lifecycleController::enableModule);
	}

	@Override
	public void unloadModules() {
		logger.info("Unloading modules...");
		modules.forEach(lifecycleController::disableModule);
		modules.forEach(lifecycleController::unloadModule);

		modules.removeIf(module -> !module.getState().equals(ModuleState.UNLOADED));
		modules.forEach(module -> logger.warn("Module was not unloaded: {}", module.getName()));
	}

	@Override
	public void reloadModules() {
		unloadModules();
		loadModules();
	}

	private void discoverModules() {
		try (Stream<Path> paths = Files.list(modulesPath)) {
			List<File> moduleFiles = paths
					.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".jar"))
					.map(Path::toFile)
					.toList();

			moduleFiles.forEach(file -> {
				try (JarFile jarFile = new JarFile(file)) {
					JarEntry entry = jarFile.getJarEntry(MODULE_FILE);
					if (entry == null) {
						logger.warn("Module file does not contain module.json file: {}", file.getName());
						return;
					}

					try (InputStream stream = jarFile.getInputStream(entry)) {
						Module module = configurationLoader.load(stream, Module.class);
						if (!validateModule(module, file))
							return;

						InternalModule internalModule = new InternalModule(
								file.toPath(),
								null,
								ModuleState.UNKNOWN,
								module
						);

						modules.add(internalModule);
					}
				} catch (IOException e) {
					logger.warn("Failed to load module from file: {}", file.getName());
				}
			});
		} catch (IOException e) {
			logger.warn("Failed to load modules from directory: {}", modulesPath);
		}
	}

	private boolean validateModule(Module module, File moduleJson) {
		if (module == null) {
			logger.warn("Failed to open module.json file: {}", moduleJson.getName());
			return false;
		}

		if (module.getName() == null || module.getVersion() == null || module.getMain() == null) {
			logger.warn("Module file is missing required fields: {}", moduleJson.getName());
			return false;
		}

		if (modules.stream().anyMatch(internalModule -> internalModule.getName().equals(module.getName()))) {
			logger.warn("Module with name already exists: {}", module.getName());
			return false;
		}

		return true;
	}
}