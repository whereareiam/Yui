package me.whereareiam.yui.adapter.plugin.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

@Component
public class PluginClassLoaderFactory {
	private final ClassLoader parent;

	@Autowired
	public PluginClassLoaderFactory(ApplicationContext ctx) {
		this.parent = ctx.getClassLoader();
	}

	public URLClassLoader create(Path jar) throws Exception {
		URL url = jar.toUri().toURL();
		return new URLClassLoader(new URL[]{url}, parent) {
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
}
