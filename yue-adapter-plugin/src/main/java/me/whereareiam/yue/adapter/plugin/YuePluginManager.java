package me.whereareiam.yue.adapter.plugin;

import me.whereareiam.yue.adapter.plugin.bean.PluginBeanRegistry;
import me.whereareiam.yue.adapter.plugin.descriptor.PluginDescriptorReader;
import me.whereareiam.yue.adapter.plugin.factory.PluginClassLoaderFactory;
import me.whereareiam.yue.adapter.plugin.factory.PluginContextFactory;
import me.whereareiam.yue.api.model.plugin.InternalPlugin;
import me.whereareiam.yue.api.model.plugin.Plugin;
import me.whereareiam.yue.api.output.plugin.PluginManager;
import me.whereareiam.yue.api.output.plugin.YuePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
public class YuePluginManager implements PluginManager {
	private static final Logger LOG = LoggerFactory.getLogger(YuePluginManager.class);

	private final Path pluginsPath;
	private final PluginStorage storage;
	private final PluginDescriptorReader descriptorReader;
	private final PluginClassLoaderFactory classLoaderFactory;
	private final PluginContextFactory contextFactory;
	private final PluginBeanRegistry registry;

	private final ReentrantLock lock = new ReentrantLock(true);

	public YuePluginManager(
			@Qualifier("pluginsPath") Path pluginsPath,
			PluginStorage storage,
			PluginDescriptorReader descriptorReader,
			PluginClassLoaderFactory classLoaderFactory,
			PluginContextFactory contextFactory,
			PluginBeanRegistry registry
	) {
		this.storage = storage;
		this.pluginsPath = pluginsPath;
		this.descriptorReader = descriptorReader;
		this.classLoaderFactory = classLoaderFactory;
		this.contextFactory = contextFactory;
		this.registry = registry;
	}

	@Override
	public void initialize() {
		try (Stream<Path> jars = Files.list(pluginsPath)) {
			jars.filter(p -> p.toString().endsWith(".jar")).forEach(this::load);
		} catch (Exception e) {
			LOG.error("Failed to load plugins", e);
		}
	}

	@Override
	public void load(Path path) {
		if (Files.notExists(path)) return;
		lock.lock();
		try {
			Plugin plugin = descriptorReader.read(path);

			if (storage.byId(plugin.getId()).isPresent()) return;

			URLClassLoader loader = classLoaderFactory.create(path);
			AnnotationConfigApplicationContext pluginCtx = contextFactory.build(loader, plugin);

			YuePlugin bean = pluginCtx.getBean(YuePlugin.class);
			bean.onLoad();

			storage.add(new InternalPlugin(plugin, loader, pluginCtx, bean));
		} catch (Exception e) {
			LOG.error("Failed loading plugin {}", path, e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public <T> void injectBean(String beanName, Class<T> beanClass, Supplier<T> supplier) {
		lock.lock();
		try {
			registry.register(beanName, beanClass, supplier);

			storage.all().forEach(obj -> {
				if (obj instanceof InternalPlugin p) {
					registry.apply(p.getContext());
					p.getContext().refresh();
				}
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void removeInjectedBean(String beanName) {
		lock.lock();
		try {
			registry.remove(beanName);

			storage.all().forEach(obj -> {
				if (obj instanceof InternalPlugin p) {
					if (p.getContext().containsBean(beanName)) {
						p.getContext().removeBeanDefinition(beanName);
						p.getContext().refresh();
					}
				}
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<YuePlugin> enable(String id) {
		lock.lock();
		try {
			return storage.byId(id).flatMap(p -> {
				if (p.isEnabled()) return Optional.of(p.getYuePlugin());
				p.getContext().refresh();
				p.getYuePlugin().onEnable();
				p.setEnabled(true);
				return Optional.of(p.getYuePlugin());
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<YuePlugin> disable(String id) {
		lock.lock();
		try {
			return storage.byId(id).flatMap(p -> {
				if (!p.isEnabled()) return Optional.of(p.getYuePlugin());
				p.getYuePlugin().onDisable();
				p.getContext().stop();
				p.setEnabled(false);
				return Optional.of(p.getYuePlugin());
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<YuePlugin> unload(String id) {
		lock.lock();
		try {
			return storage.byId(id).flatMap(p -> {
				if (p.isEnabled()) {
					p.getYuePlugin().onDisable();
					p.getContext().stop();
					p.setEnabled(false);
				}
				p.getYuePlugin().onUnload();
				p.getContext().close();
				close(p.getClassLoader());
				storage.remove(id);
				return Optional.of(p.getYuePlugin());
			});
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
		} catch (Exception ignored) {
		}
	}
}