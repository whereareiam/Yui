package me.whereareiam.yue.adapter.plugin;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yue.adapter.plugin.bean.PluginBeanRegistry;
import me.whereareiam.yue.adapter.plugin.descriptor.PluginDescriptorReader;
import me.whereareiam.yue.adapter.plugin.factory.PluginClassLoaderFactory;
import me.whereareiam.yue.adapter.plugin.factory.PluginContextFactory;
import me.whereareiam.yue.api.event.plugin.PluginDisabledEvent;
import me.whereareiam.yue.api.event.plugin.PluginEnabledEvent;
import me.whereareiam.yue.api.event.plugin.PluginLoadedEvent;
import me.whereareiam.yue.api.event.plugin.PluginUnloadedEvent;
import me.whereareiam.yue.api.model.plugin.InternalPlugin;
import me.whereareiam.yue.api.model.plugin.Plugin;
import me.whereareiam.yue.api.output.plugin.PluginManager;
import me.whereareiam.yue.api.output.plugin.YuePlugin;
import me.whereareiam.yue.api.type.PluginState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
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

@Slf4j
@Service
public class YuePluginManager implements PluginManager {
	private final Path pluginsPath;
	private final PluginStorage storage;
	private final PluginDescriptorReader descriptorReader;
	private final PluginClassLoaderFactory classLoaderFactory;
	private final PluginContextFactory contextFactory;
	private final PluginBeanRegistry registry;
	private final ApplicationEventPublisher eventPublisher;

	private final ReentrantLock lock = new ReentrantLock(true);

	public YuePluginManager(
			@Qualifier("pluginsPath") Path pluginsPath,
			PluginStorage storage,
			PluginDescriptorReader descriptorReader,
			PluginClassLoaderFactory classLoaderFactory,
			PluginContextFactory contextFactory,
			PluginBeanRegistry registry,
			ApplicationEventPublisher eventPublisher
	) {
		this.storage = storage;
		this.pluginsPath = pluginsPath;
		this.descriptorReader = descriptorReader;
		this.classLoaderFactory = classLoaderFactory;
		this.contextFactory = contextFactory;
		this.registry = registry;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void initialize() {
		try (Stream<Path> jars = Files.list(pluginsPath)) {
			jars.filter(p -> p.toString().endsWith(".jar")).forEach(this::load);
		} catch (Exception e) {
			log.error("Failed to load plugins", e);
		}

		storage.all().forEach(p -> enable(p.getPlugin().getId()));
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

			InternalPlugin internalPlugin = new InternalPlugin(plugin, loader, pluginCtx, bean);
			PluginLoadedEvent event = new PluginLoadedEvent(internalPlugin);
			eventPublisher.publishEvent(event);

			if (!event.isCancelled())
				bean.onLoad();

			storage.add(internalPlugin);
		} catch (Exception e) {
			log.error("Failed loading plugin {}", path, e);
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
					safeRefresh(p.getContext());
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
						safeRefresh(p.getContext());
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

				PluginEnabledEvent event = new PluginEnabledEvent(p);
				eventPublisher.publishEvent(event);
				if (event.isCancelled()) return Optional.empty();

				p.getContext().start();
				p.getYuePlugin().onEnable();
				p.setState(PluginState.ENABLED);

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

				PluginDisabledEvent event = new PluginDisabledEvent(p);
				eventPublisher.publishEvent(event);
				if (event.isCancelled()) return Optional.empty();

				p.getYuePlugin().onDisable();
				p.getContext().stop();
				p.setState(PluginState.DISABLED);

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
				if (p.isEnabled())
					disable(id);

				PluginUnloadedEvent event = new PluginUnloadedEvent(p);
				eventPublisher.publishEvent(event);
				if (event.isCancelled()) return Optional.empty();

				p.getYuePlugin().onUnload();
				p.setState(PluginState.UNLOADED);
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
	public Collection<InternalPlugin> plugins() {
		return storage.all();
	}

	private void close(URLClassLoader cl) {
		try {
			cl.close();
		} catch (Exception ignored) {
		}
	}

	private void safeRefresh(AnnotationConfigApplicationContext ctx) {
		if (!ctx.isActive())
			ctx.refresh();
	}
}