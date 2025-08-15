package me.whereareiam.yui.adapter.plugin.factory;

import me.whereareiam.yui.adapter.plugin.PluginStorage;
import me.whereareiam.yui.adapter.plugin.loader.PluginClassLoader;
import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class PluginClassLoaderFactory {
	private final ClassLoader parent;
	private final PluginStorage storage;

	@Autowired
	public PluginClassLoaderFactory(
			ApplicationContext ctx,
			PluginStorage storage
	) {
		this.parent = ctx.getClassLoader();
		this.storage = storage;
	}

	/**
	 * Creates a class‑loader for {@code jar} that can also delegate
	 * to the supplied {@code dependencyLoaders}.
	 */
	public URLClassLoader create(
			Path jar,
			List<ClassLoader> dependencyLoaders
	) throws Exception {
		URL url = jar.toUri().toURL();

		// Build a comprehensive delegation list: declared dependencies + all existing plugin loaders.
		List<ClassLoader> delegates = new ArrayList<>(dependencyLoaders);
		for (InternalPlugin p : storage.all()) {
			ClassLoader cl = p.getClassLoader();
			if (!delegates.contains(cl))
				delegates.add(cl);
		}

		return new PluginClassLoader(new URL[]{url}, parent, delegates);
	}
}