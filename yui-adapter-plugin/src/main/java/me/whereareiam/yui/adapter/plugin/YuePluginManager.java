package me.whereareiam.yui.adapter.plugin;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.plugin.bean.PluginBeanRegistry;
import me.whereareiam.yui.adapter.plugin.descriptor.PluginDescriptorReader;
import me.whereareiam.yui.adapter.plugin.factory.PluginClassLoaderFactory;
import me.whereareiam.yui.adapter.plugin.factory.PluginContextFactory;
import me.whereareiam.yui.api.event.plugin.PluginDisabledEvent;
import me.whereareiam.yui.api.event.plugin.PluginEnabledEvent;
import me.whereareiam.yui.api.event.plugin.PluginLoadedEvent;
import me.whereareiam.yui.api.event.plugin.PluginUnloadedEvent;
import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import me.whereareiam.yui.api.model.plugin.Plugin;
import me.whereareiam.yui.api.output.plugin.PluginManager;
import me.whereareiam.yui.api.output.plugin.YuiPlugin;
import me.whereareiam.yui.api.type.PluginState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@Service
public class YuiPluginManager implements PluginManager {
	private final Path pluginsPath;
	private final PluginStorage storage;
	private final PluginDescriptorReader descriptorReader;
	private final PluginClassLoaderFactory classLoaderFactory;
	private final PluginContextFactory contextFactory;
	private final PluginBeanRegistry registry;
	private final ApplicationEventPublisher eventPublisher;

	private final ReentrantLock lock = new ReentrantLock(true);

	public YuiPluginManager(
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
		lock.lock();
		try {
			Map<String, Path> jarById = new HashMap<>();
			Map<String, Plugin> descriptorById = new HashMap<>();

			try (Stream<Path> jars = Files.list(pluginsPath)) {
				jars.filter(p -> p.toString().endsWith(".jar")).forEach(jar -> {
					try {
						Plugin plugin = descriptorReader.read(jar);
						jarById.put(plugin.getId(), jar);
						descriptorById.put(plugin.getId(), plugin);
					} catch (Exception e) {
						log.error("Failed reading descriptor for {}", jar, e);
					}
				});
			}

			List<String> orderedIds = topologicalSort(descriptorById);

			orderedIds.forEach(id -> load(jarById.get(id)));
		} catch (Exception e) {
			log.error("Failed to initialise plugins", e);
		} finally {
			lock.unlock();
		}

		storage.all().forEach(p -> enable(p.getPlugin().getId()));
	}

	private List<String> topologicalSort(Map<String, Plugin> plugins) {
		Map<String, List<String>> adj = new HashMap<>();
		Set<String> skipped = new HashSet<>();

		plugins.forEach((id, plugin) -> {
			List<String> deps = new ArrayList<>();
			if (plugin.getDependencies() != null)
				plugin.getDependencies().forEach(d -> {
					if (!plugins.containsKey(d.getId())) {
						if (d.isRequired()) return;
						log.error("Plugin {} is missing required dependency {}", id, d.getId());
						skipped.add(id);
						return;
					}
					deps.add(d.getId());
				});
			adj.put(id, deps);
		});

		List<String> sorted = new ArrayList<>();
		Set<String> visiting = new HashSet<>();
		Set<String> visited = new HashSet<>();

		for (String id : adj.keySet()) {
			if (skipped.contains(id) || visited.contains(id)) continue;
			if (dfs(id, adj, visiting, visited, sorted)) {
				log.error("Circular dependency detected while loading plugins");
				return Collections.emptyList();
			}
		}
		Collections.reverse(sorted);

		return sorted.stream().filter(id -> !skipped.contains(id)).toList();
	}

	private boolean dfs(String id, Map<String, List<String>> adj, Set<String> visiting, Set<String> visited, List<String> out) {
		if (visiting.contains(id)) return true;
		if (visited.contains(id)) return false;

		visiting.add(id);
		for (String dep : adj.getOrDefault(id, List.of()))
			if (dfs(dep, adj, visiting, visited, out)) return true;

		visiting.remove(id);
		visited.add(id);
		out.add(id);

		return false;
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
			YuiPlugin bean = pluginCtx.getBean(YuiPlugin.class);

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
	public Optional<YuiPlugin> enable(String id) {
		lock.lock();
		try {
			return storage.byId(id).flatMap(p -> {
				if (p.isEnabled()) return Optional.of(p.getYuiPlugin());

				PluginEnabledEvent event = new PluginEnabledEvent(p);
				eventPublisher.publishEvent(event);
				if (event.isCancelled()) return Optional.empty();

				p.getContext().start();
				p.getYuiPlugin().onEnable();
				p.setState(PluginState.ENABLED);

				return Optional.of(p.getYuiPlugin());
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<YuiPlugin> disable(String id) {
		lock.lock();
		try {
			return storage.byId(id).flatMap(p -> {
				if (!p.isEnabled()) return Optional.of(p.getYuiPlugin());

				PluginDisabledEvent event = new PluginDisabledEvent(p);
				eventPublisher.publishEvent(event);
				if (event.isCancelled()) return Optional.empty();

				p.getYuiPlugin().onDisable();
				p.getContext().stop();
				p.setState(PluginState.DISABLED);

				return Optional.of(p.getYuiPlugin());
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<YuiPlugin> unload(String id) {
		lock.lock();
		try {
			return storage.byId(id).flatMap(p -> {
				if (p.isEnabled())
					disable(id);

				PluginUnloadedEvent event = new PluginUnloadedEvent(p);
				eventPublisher.publishEvent(event);
				if (event.isCancelled()) return Optional.empty();

				p.getYuiPlugin().onUnload();
				p.setState(PluginState.UNLOADED);
				p.getContext().close();
				close(p.getClassLoader());
				storage.remove(id);

				return Optional.of(p.getYuiPlugin());
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