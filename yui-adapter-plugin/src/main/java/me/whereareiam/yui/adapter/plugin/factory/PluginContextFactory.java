package me.whereareiam.yui.adapter.plugin.factory;

import me.whereareiam.yui.adapter.plugin.PluginInteractionService;
import me.whereareiam.yui.adapter.plugin.bean.PluginBeanRegistry;
import me.whereareiam.yui.api.input.InteractionService;
import me.whereareiam.yui.api.model.plugin.Plugin;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
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
		childContext.registerBean("pluginId", String.class, plugin::getId);

		childContext.registerBean(InteractionService.class, () -> new PluginInteractionService(plugin.getId(), parent.getBean(InteractionService.class)));

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

				// Bridge parent events to child context and ensure proper cleanup on context close
				ApplicationListener<ApplicationEvent> bridge = childMulticaster::multicastEvent;
				parentMulticaster.addApplicationListener(bridge);
				childContext.registerBean("pluginEventBridgeCleanup", DisposableBean.class,
						() -> () -> parentMulticaster.removeApplicationListener(bridge));
			} catch (Exception e) {
				// Log but don't fail the entire plugin load
				// This is not critical for plugin functionality
			}
		}

		return childContext;
	}

	public void cleanupParentSingletons(ClassLoader pluginClassLoader) {
		if (!(parent instanceof ConfigurableApplicationContext configurableParent)) return;
		ConfigurableListableBeanFactory beanFactory = configurableParent.getBeanFactory();
		if (!(beanFactory instanceof DefaultListableBeanFactory dlbf)) return;

		for (String name : dlbf.getSingletonNames()) {
			Object singleton;
			try {
				singleton = dlbf.getSingleton(name);
			} catch (Exception ignored) {
				continue;
			}
			if (singleton == null) continue;
			ClassLoader cl = singleton.getClass().getClassLoader();
			if (isFromPluginClassLoader(cl, pluginClassLoader)) {
				try {
					dlbf.destroySingleton(name);
				} catch (Exception ignored) {
				}
			}
		}
	}

	private boolean isFromPluginClassLoader(ClassLoader candidate, ClassLoader pluginClassLoader) {
		if (candidate == null) return false;
		ClassLoader cl = candidate;
		while (cl != null) {
			if (cl == pluginClassLoader) return true;
			cl = cl.getParent();
		}
		return false;
	}
}
