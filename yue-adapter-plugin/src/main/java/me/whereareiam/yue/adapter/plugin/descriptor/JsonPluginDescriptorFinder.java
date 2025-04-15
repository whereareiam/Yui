package me.whereareiam.yue.adapter.plugin.descriptor;

import me.whereareiam.yue.api.model.YuePluginDescriptor;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JsonPluginDescriptorFinder implements PluginDescriptorFinder {
	private static final String DEFAULT_DESCRIPTOR_FILE = "plugin.json";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ConfigurationLoader configurationLoader;

	public JsonPluginDescriptorFinder(ConfigurationLoader configurationLoader) {
		this.configurationLoader = configurationLoader;
	}

	@Override
	public boolean isApplicable(Path pluginPath) {
		return FileUtils.isJarFile(pluginPath);
	}

	@Override
	public PluginDescriptor find(Path pluginPath) {
		try (ZipFile zipFile = new ZipFile(pluginPath.toFile())) {
			ZipEntry entry = zipFile.getEntry(DEFAULT_DESCRIPTOR_FILE);

			if (entry == null) {
				logger.error("No {} found inside JAR {}", DEFAULT_DESCRIPTOR_FILE, pluginPath);
				throw new IllegalArgumentException("Cannot find plugin.json descriptor");
			}

			try (InputStream input = zipFile.getInputStream(entry)) {
				YuePluginDescriptor descriptor = configurationLoader.load(input, YuePluginDescriptor.class);

				if (descriptor.getName() == null || descriptor.getPluginClass() == null) {
					logger.error("Invalid descriptor: {}", pluginPath);
					throw new IllegalArgumentException("Invalid descriptor: descriptor");
				}

				return descriptor;
			}

		} catch (IOException e) {
			logger.error("Error reading plugin descriptor from {}", pluginPath, e);
			throw new IllegalStateException("Error reading plugin descriptor", e);
		}
	}
}
