package me.whereareiam.yue.adapter.plugin;

import me.whereareiam.yue.api.exception.PluginLoadException;
import me.whereareiam.yue.api.model.plugin.InternalPlugin;
import me.whereareiam.yue.api.model.plugin.Plugin;
import me.whereareiam.yue.api.output.config.ConfigurationLoader;
import me.whereareiam.yue.api.output.plugin.PluginManager;
import me.whereareiam.yue.api.output.plugin.YuePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Component
public class YuePluginManager implements PluginManager {
	private static final Logger LOG = LoggerFactory.getLogger(YuePluginManager.class);

	private final ApplicationContext ctx;
	private final ConfigurationLoader configLoader;
	private final PluginStorage storage;
	private final Path pluginsPath;

	private final ReentrantLock lock = new ReentrantLock(true);

	public YuePluginManager(
			ApplicationContext ctx,
			ConfigurationLoader configLoader,
			PluginStorage storage,
			@Qualifier("pluginsPath") Path pluginsPath
	) {
		this.ctx = ctx;
		this.configLoader = configLoader;
		this.storage = storage;
		this.pluginsPath = pluginsPath;
	}

	@Override
	public void initialize() {
		try (Stream<Path> jars = Files.list(pluginsPath)) {
			jars.filter(p -> p.toString().endsWith(".jar"))
					.forEach(this::load);
		} catch (Exception e) {
			LOG.error("Failed to load plugins from {}", pluginsPath, e);
		}
	}

	@Override
	public void load(Path path) {
		if (Files.notExists(path)) {
			LOG.warn("Plugin path not found: {}", path);
			return;
		}

		lock.lock();

		try {
			Plugin plugin = readPlugin(path);
			if (storage.byId(plugin.getId()).isPresent()) {
				LOG.info("Plugin {} already loaded", plugin.getId());
				return;
			}

			URLClassLoader classLoader = createClassLoader(path);

			AnnotationConfigApplicationContext childContext = buildPluginContext(classLoader, plugin);

			YuePlugin pluginBean = childContext.getBean(YuePlugin.class);
			pluginBean.onLoad();

			storage.add(new InternalPlugin(plugin, classLoader, childContext, pluginBean));

			LOG.info("Loaded plugin {}", plugin.getId());
		} catch (Exception e) {
			LOG.error("Failed to load plugin from {}", path, e);
		} finally {
			lock.unlock();
		}
	}

	private Plugin readPlugin(Path jar) throws Exception {
		try (FileSystem fs = FileSystems.newFileSystem(jar);
		     InputStream in = Files.newInputStream(fs.getPath("/plugin.json"))) {
			Plugin plugin = configLoader.load(in, Plugin.class);

			if (plugin.getId() == null || plugin.getName() == null || plugin.getEntrypoint() == null) {
				throw new PluginLoadException("Plugin missing required fields: " +
						"id=" + plugin.getId() +
						", name=" + plugin.getName() +
						", entrypoint=" + plugin.getEntrypoint());
			}

			return plugin;
		}
	}

	private URLClassLoader createClassLoader(Path jar) throws Exception {
		URL url = jar.toUri().toURL();

		return new URLClassLoader(new URL[]{url}, ctx.getClassLoader()) {
			@Override
			protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				try {
					return findClass(name);
				} catch (ClassNotFoundException ex) {
					return super.loadClass(name, resolve);
				}
			}
		};
	}

	private AnnotationConfigApplicationContext buildPluginContext(ClassLoader pluginClassLoader, Plugin plugin) throws ClassNotFoundException {
		String entrypoint = plugin.getEntrypoint();
		String basePackage = entrypoint.substring(0, entrypoint.lastIndexOf('.'));

		Class<?> pluginMainClass = Class.forName(entrypoint, true, pluginClassLoader);

		AnnotationConfigApplicationContext childContext = new AnnotationConfigApplicationContext();
		childContext.setParent(ctx);
		childContext.setClassLoader(pluginClassLoader);
		childContext.registerBean(pluginMainClass);
		childContext.scan(basePackage);

		// inject plugin-specific beans
		childContext.registerBean("pluginPath", Path.class, () -> pluginsPath.resolve(plugin.getId()));

		childContext.refresh();

		return childContext;
	}

	@Override
	public Optional<YuePlugin> enable(String id) {
		lock.lock();
		try {
			return storage.byId(id).flatMap(plugin -> {
				if (plugin.isEnabled()) return
						Optional.of(plugin.getYuePlugin());

				plugin.getContext().refresh();
				plugin.getYuePlugin().onEnable();
				plugin.setEnabled(true);

				LOG.info("Enabled plugin {}", id);

				return Optional.of(plugin.getYuePlugin());
			});
		} catch (Exception e) {
			LOG.error("Error enabling plugin {}", id, e);
			return Optional.empty();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<YuePlugin> disable(String id) {
		lock.lock();
		try {
			return storage.byId(id).flatMap(plugin -> {
				if (!plugin.isEnabled())
					return Optional.of(plugin.getYuePlugin());

				plugin.getYuePlugin().onDisable();
				plugin.getContext().stop();
				plugin.setEnabled(false);

				LOG.info("Disabled plugin {}", id);

				return Optional.of(plugin.getYuePlugin());
			});
		} catch (Exception e) {
			LOG.error("Error disabling plugin {}", id, e);
			return Optional.empty();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<YuePlugin> unload(String id) {
		lock.lock();
		try {
			return storage.byId(id).flatMap(plugin -> {
				if (plugin.isEnabled()) {
					plugin.getYuePlugin().onDisable();
					plugin.getContext().stop();
					plugin.setEnabled(false);

					LOG.info("Auto-disabled {} before unload", id);
				}

				plugin.getYuePlugin().onUnload();
				plugin.getContext().close();
				close(plugin.getClassLoader());
				storage.remove(id);

				LOG.info("Unloaded plugin {}", id);

				return Optional.of(plugin.getYuePlugin());
			});
		} catch (Exception e) {
			LOG.error("Error unloading plugin {}", id, e);
			return Optional.empty();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<InternalPlugin> whichPlugin(Class<?> type) {
		return storage.byType(type);
	}

	@Override
	public Collection<?> plugins() {
		return storage.all();
	}

	private void close(URLClassLoader cl) {
		try {
			cl.close();
		} catch (Exception e) {
			LOG.warn("Failed closing classloader", e);
		}
	}
}
