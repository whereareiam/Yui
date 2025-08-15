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
import me.whereareiam.yui.api.input.Registry;
import me.whereareiam.yui.api.model.plugin.Dependency;
import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import me.whereareiam.yui.api.model.plugin.Plugin;
import me.whereareiam.yui.api.output.Reloadable;
import me.whereareiam.yui.api.output.plugin.PluginManager;
import me.whereareiam.yui.api.output.plugin.YuiPlugin;
import me.whereareiam.yui.api.type.PluginState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@Service
public class YuiPluginManager implements PluginManager, Reloadable {
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
			ApplicationEventPublisher eventPublisher,
			Registry<Reloadable> reloadableRegistry
	) {
		this.storage = storage;
		this.pluginsPath = pluginsPath;
		this.descriptorReader = descriptorReader;
		this.classLoaderFactory = classLoaderFactory;
		this.contextFactory = contextFactory;
		this.registry = registry;
		this.eventPublisher = eventPublisher;

		reloadableRegistry.register(this);
	}

	@Override
	public void initialize() {
		lock.lock();
		try {
			Map<String, Plugin> loadable = loadable();
			Map<String, Plugin> descriptorById = new HashMap<>();
			loadable.values().forEach(p -> descriptorById.put(p.getId(), p));

			List<String> orderedIds = topologicalSort(descriptorById);

			orderedIds.forEach(id -> loadable.entrySet().stream()
					.filter(e -> id.equals(e.getValue().getId()))
					.map(Map.Entry::getKey)
					.findFirst().ifPresent(this::load));
		} catch (Exception e) {
			log.error("[PluginManager]: Failed to initialise plugins", e);
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
						if (d.isRequired()) {
							log.error("[PluginManager]: Plugin {} is missing required dependency {}", id, d.getId());
							skipped.add(id);
						}
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
				log.error("[PluginManager]: Circular dependency detected while loading plugins");
				return Collections.emptyList();
			}
		}

		return sorted.stream()
				.filter(id -> !skipped.contains(id))
				.toList();
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

			if (storage.byId(plugin.getId()).isPresent())
				return;

			List<ClassLoader> deps = Optional.ofNullable(plugin.getDependencies())
					.orElse(List.of())
					.stream()
					.filter(Dependency::isInjectClassLoader)
					.map(d -> storage.byId(d.getId()).orElse(null))
					.filter(Objects::nonNull)
					.map(InternalPlugin::getClassLoader)
					.toList();

			URLClassLoader loader = classLoaderFactory.create(path, deps);

			AnnotationConfigApplicationContext pluginCtx =
					contextFactory.build(loader, plugin);

			YuiPlugin bean = pluginCtx.getBean(YuiPlugin.class);
			InternalPlugin internal = new InternalPlugin(plugin, loader, pluginCtx, bean);

			log.info("[PluginManager]: Loading plugin {} [v{}]", plugin.getName(), plugin.getVersion());

			eventPublisher.publishEvent(new PluginLoadedEvent(internal));

			bean.onLoad();
			storage.add(internal);
		} catch (Exception e) {
			log.error("[PluginManager]: Failed loading plugin {}", path, e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void load(String jarName) {
		if (jarName == null || jarName.isBlank()) return;
		String fileName = jarName.endsWith(".jar") ? jarName : jarName + ".jar";
		Path path = pluginsPath.resolve(fileName);
		load(path);
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
				if (p.isEnabled()) return Optional.of(p.getInstance());

				log.info("[PluginManager]: Enabling plugin {} [v{}]", p.getPlugin().getName(), p.getPlugin().getVersion());

				PluginEnabledEvent event = new PluginEnabledEvent(p);
				eventPublisher.publishEvent(event);
				if (event.isCancelled()) return Optional.empty();

				p.getContext().start();
				p.getInstance().onEnable();
				p.setState(PluginState.ENABLED);

				return Optional.of(p.getInstance());
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
				if (!p.isEnabled()) return Optional.of(p.getInstance());

				log.info("[PluginManager]: Disabling plugin {} [v{}]", p.getPlugin().getName(), p.getPlugin().getVersion());

				PluginDisabledEvent event = new PluginDisabledEvent(p);
				eventPublisher.publishEvent(event);
				if (event.isCancelled()) return Optional.empty();

				p.getInstance().onDisable();
				p.getContext().stop();
				p.setState(PluginState.DISABLED);

				return Optional.of(p.getInstance());
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

				log.info("[PluginManager]: Unloading plugin {} [v{}]", p.getPlugin().getName(), p.getPlugin().getVersion());

				PluginUnloadedEvent event = new PluginUnloadedEvent(p);
				eventPublisher.publishEvent(event);
				if (event.isCancelled()) return Optional.empty();

				p.getInstance().onUnload();
				p.setState(PluginState.UNLOADED);
				p.getContext().close();
				contextFactory.cleanupParentSingletons(p.getClassLoader());
				close(p.getClassLoader());
				storage.remove(id);

				return Optional.of(p.getInstance());
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

	@Override
	public Map<String, Plugin> loadable() {
		lock.lock();
		try {
			Map<String, Plugin> result = new LinkedHashMap<>();
			if (Files.notExists(pluginsPath)) return result;
			try {
				try (Stream<Path> files = Files.list(pluginsPath)) {
					files.filter(p -> p.toString().endsWith(".jar"))
							.forEach(jar -> {
								try {
									Plugin plugin = descriptorReader.read(jar);
									if (storage.byId(plugin.getId()).isPresent()) return; // already loaded
									String base = jar.getFileName().toString();
									base = base.endsWith(".jar") ? base.substring(0, base.length() - 4) : base;
									result.put(base, plugin);
								} catch (Exception e) {
									log.debug("[PluginManager]: Skipping non-plugin jar {}: {}", jar, e.getMessage());
								}
							});
				}
			} catch (IOException e) {
				log.debug("[PluginManager]: Failed to scan plugins directory {}: {}", pluginsPath, e.getMessage());
			}
			return result;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void reload() {
		lock.lock();
		try {
			// Step 1: Get all plugin IDs before unloading
			List<String> pluginIds = storage.all().stream()
					.map(p -> p.getPlugin().getId())
					.toList();

			log.debug("[PluginManager]: Unloading {} plugins", pluginIds.size());

			// Step 2: Unload all plugins (this will disable them first if needed)
			// The unload method already handles disabling, cleanup, and storage removal
			pluginIds.forEach(id -> {
				try {
					unload(id);
					log.debug("[PluginManager]: Unloaded plugin: {}", id);
				} catch (Exception e) {
					log.error("[PluginManager]: Failed to unload plugin: {}", id, e);
				}
			});

			// Step 3: Reinitialize all plugins from disk
			log.debug("[PluginManager]: Reinitializing plugins from disk");
			initialize();
		} catch (Exception e) {
			log.error("[PluginManager]: Failed to reload plugin system", e);
		} finally {
			lock.unlock();
		}
	}

	private void close(ClassLoader cl) {
		if (cl instanceof URLClassLoader url) {
			try {
				url.close();
			} catch (Exception ignored) {
			}
		}
	}

	private void safeRefresh(AnnotationConfigApplicationContext ctx) {
		if (!ctx.isActive())
			ctx.refresh();
	}
}