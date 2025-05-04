package me.whereareiam.yui.adapter.plugin;

import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PluginStorage {
	private final Map<String, InternalPlugin> byId = new HashMap<>();
	private final Map<ClassLoader, InternalPlugin> byLoader = new HashMap<>();

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
		return Optional.ofNullable(byLoader.get(c.getClassLoader()));
	}

	public Collection<InternalPlugin> all() {
		return Collections.unmodifiableCollection(byId.values());
	}
}
