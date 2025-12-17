package me.whereareiam.yui.adapter.plugin;

import me.whereareiam.yui.model.plugin.InternalPlugin;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PluginStorage {
	private final Map<String, InternalPlugin> byId = new ConcurrentHashMap<>();
	private final Map<ClassLoader, InternalPlugin> byLoader = new ConcurrentHashMap<>();

	public void add(InternalPlugin p) {
		byId.put(p.getPlugin().getId(), p);
		byLoader.put(p.getClassLoader(), p);
	}

	public void remove(String id) {
		InternalPlugin p = byId.remove(id);
		if (p != null) byLoader.remove(p.getClassLoader());
	}

	public Optional<InternalPlugin> byId(String id) {
		return Optional.ofNullable(byId.get(id));
	}

	public Optional<InternalPlugin> byType(Class<?> c) {
		ClassLoader loader = c.getClassLoader();
		if (loader == null) return Optional.empty();
		
		return Optional.ofNullable(byLoader.get(loader));
	}

	public Collection<InternalPlugin> all() {
		return Collections.unmodifiableCollection(byId.values());
	}
}
