package me.whereareiam.yui.adapter.plugin.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;

/**
 * A class‑loader that first looks in its own URLs, then
 * in a list of dependency class‑loaders, and finally
 * in the normal parent hierarchy.
 */
public final class PluginClassLoader extends URLClassLoader {
	private final List<ClassLoader> dependencyLoaders;

	public PluginClassLoader(
			URL[] urls,
			ClassLoader parent,
			List<ClassLoader> dependencyLoaders
	) {
		super(urls, parent);
		this.dependencyLoaders = Objects.requireNonNull(dependencyLoaders);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {

		// Prefer dependency loaders for classes under a ".api." package to ensure
		// a single shared API type across plugins and avoid ClassCastException.
		boolean isApi = name != null && name.contains(".api.");

		Class<?> existing = findLoadedClass(name);
		if (existing != null) return existing;

		if (isApi) {
			for (ClassLoader dep : dependencyLoaders) {
				try {
					return dep.loadClass(name);
				} catch (ClassNotFoundException ignored) {
				}
			}
		}

		try {
			return super.loadClass(name, resolve);
		} catch (ClassNotFoundException ignored) {
		}

		if (!isApi) {
			for (ClassLoader dep : dependencyLoaders) {
				try {
					return dep.loadClass(name);
				} catch (ClassNotFoundException ignored) {
				}
			}
		}

		throw new ClassNotFoundException(name);
	}

	@Override
	public URL getResource(String name) {
		boolean isApi = name != null && name.contains("/api/");

		if (isApi) {
			for (ClassLoader dep : dependencyLoaders) {
				URL url = dep.getResource(name);
				if (url != null) return url;
			}
		}

		URL url = super.getResource(name);
		if (url != null) return url;

		if (!isApi) {
			for (ClassLoader dep : dependencyLoaders) {
				url = dep.getResource(name);
				if (url != null) return url;
			}
		}
		return null;
	}
}
