package me.whereareiam.yui.adapter.plugin.factory;

import me.whereareiam.yui.adapter.plugin.bean.PluginBeanRegistry;
import me.whereareiam.yui.api.model.plugin.Plugin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class PluginContextFactory {
	private final ApplicationContext parent;
	private final PluginBeanRegistry registry;

	private final Path pluginsPath;

	public PluginContextFactory(
			ApplicationContext parent,
			PluginBeanRegistry registry,
			@Qualifier("pluginsPath") Path pluginsPath
	) {
		this.parent = parent;
		this.registry = registry;
		this.pluginsPath = pluginsPath;
	}

	public AnnotationConfigApplicationContext build(ClassLoader pluginClassLoader, Plugin plugin) throws ClassNotFoundException {
		String entrypoint = plugin.getEntrypoint();
		String basePackage = entrypoint.substring(0, entrypoint.lastIndexOf('.'));

		Class<?> mainClass = Class.forName(entrypoint, true, pluginClassLoader);

		AnnotationConfigApplicationContext childContext = new AnnotationConfigApplicationContext();

		childContext.setParent(parent);
		childContext.setClassLoader(pluginClassLoader);
		childContext.registerBean(mainClass);
		childContext.scan(basePackage);

		childContext.registerBean("pluginPath", Path.class, () -> pluginsPath.resolve(plugin.getName()));

		// Apply registry beans before refresh
		registry.apply(childContext);

		try {
			childContext.refresh();
		} catch (Exception e) {
			// If refresh fails, close the context to prevent resource leaks
			try {
				childContext.close();
			} catch (Exception closeException) {
				// Log but don't throw - we want to preserve the original error
			}
			throw e;
		}

		// Only set up event multicasting if context is active
		if (childContext.isActive()) {
			try {
				ApplicationEventMulticaster parentMulticaster =
						parent.getBean(ApplicationEventMulticaster.class);
				ApplicationEventMulticaster childMulticaster =
						childContext.getBean(ApplicationEventMulticaster.class);

				parentMulticaster.addApplicationListener(childMulticaster::multicastEvent);
			} catch (Exception e) {
				// Log but don't fail the entire plugin load
				// This is not critical for plugin functionality
			}
		}

		return childContext;
	}
}
