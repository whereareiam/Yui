package me.whereareiam.yue.adapter.plugin.factory;

import me.whereareiam.yue.adapter.plugin.bean.PluginBeanRegistry;
import me.whereareiam.yue.api.model.plugin.Plugin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
		
		childContext.registerBean("pluginPath", Path.class, () -> pluginsPath.resolve(plugin.getId()));

		registry.apply(childContext);

		childContext.refresh();

		return childContext;
	}
}