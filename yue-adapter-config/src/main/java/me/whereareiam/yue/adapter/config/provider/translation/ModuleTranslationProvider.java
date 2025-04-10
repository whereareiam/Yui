package me.whereareiam.yue.adapter.config.provider.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import me.whereareiam.yue.api.input.translation.TranslationLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ModuleTranslationProvider implements TranslationLoader {

	private final Path modulesPath;
	private final ObjectMapper objectMapper;

	/**
	 * We store:
	 * moduleName -> (locale -> (key -> text))
	 */
	private final Map<String, Map<Locale, Map<String, String>>> moduleTranslations = new ConcurrentHashMap<>();

	public ModuleTranslationProvider(@Qualifier("modulesPath") Path modulesPath,
	                                 ObjectMapper objectMapper) {
		this.modulesPath = modulesPath;
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	public void init() {
		loadAllModules();
	}

	private void loadAllModules() {
		if (!Files.exists(modulesPath) || !Files.isDirectory(modulesPath)) {
			return;
		}

		try (DirectoryStream<Path> ds = Files.newDirectoryStream(modulesPath)) {
			for (Path moduleDir : ds) {
				if (Files.isDirectory(moduleDir)) {
					String moduleName = moduleDir.getFileName().toString();
					loadModuleTranslations(moduleName, moduleDir);
				}
			}
		} catch (IOException e) {
			// log or handle
		}
	}

	private void loadModuleTranslations(String moduleName, Path moduleDir) {
		Path langDir = moduleDir.resolve("languages");
		if (!Files.exists(langDir) || !Files.isDirectory(langDir)) {
			return;
		}

		try (DirectoryStream<Path> ds = Files.newDirectoryStream(langDir, "*.json")) {
			for (Path file : ds) {
				loadSingleFile(moduleName, file);
			}
		} catch (IOException e) {
			// log or handle
		}
	}

	@SuppressWarnings("unchecked")
	private void loadSingleFile(String moduleName, Path file) {
		String filename = file.getFileName().toString(); // e.g. "en.json"
		String localeStr = filename.substring(0, filename.lastIndexOf('.'));
		Locale locale = Locale.forLanguageTag(localeStr);

		try {
			Map<String, String> content =
					objectMapper.readValue(Files.newInputStream(file), Map.class);

			moduleTranslations
					.computeIfAbsent(moduleName, x -> new ConcurrentHashMap<>())
					.computeIfAbsent(locale, x -> new ConcurrentHashMap<>())
					.putAll(content);

		} catch (IOException e) {
			// log or handle
		}
	}

	@Override
	public Map<String, Map<Locale, Map<String, String>>> loadAll() {
		// Return everything in memory
		return moduleTranslations;
	}
}