package me.whereareiam.yui.adapter.plugin.factory;

import me.whereareiam.yui.adapter.plugin.loader.PluginClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

@Component
public class PluginClassLoaderFactory {

	private final ClassLoader parent;

	@Autowired
	public PluginClassLoaderFactory(ApplicationContext ctx) {
		this.parent = ctx.getClassLoader();
	}

	/**
	 * Creates a class‑loader for {@code jar} that can also delegate
	 * to the supplied {@code dependencyLoaders}.
	 */
	public URLClassLoader create(Path jar,
	                             List<ClassLoader> dependencyLoaders)
			throws Exception {

		URL url = jar.toUri().toURL();
		return new PluginClassLoader(new URL[]{url}, parent, dependencyLoaders);
	}
}