package me.whereareiam.yui.adapter.plugin.factory;

import me.whereareiam.yui.adapter.plugin.PluginInteractionService;
import me.whereareiam.yui.adapter.plugin.bean.DependencyBeanBridge;
import me.whereareiam.yui.adapter.plugin.bean.PluginBeanRegistry;
import me.whereareiam.yui.model.plugin.Plugin;
import me.whereareiam.yui.service.InteractionService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class PluginContextFactory {
	private final ApplicationContext parent;
	private final PluginBeanRegistry registry;
	private final DependencyBeanBridge beanBridge;

	private final Path pluginsPath;

	public PluginContextFactory(
			ApplicationContext parent,
			PluginBeanRegistry registry,
			DependencyBeanBridge beanBridge,
			@Qualifier("pluginsPath") Path pluginsPath
	) {
		this.parent = parent;
		this.registry = registry;
		this.beanBridge = beanBridge;
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

		// Bridge beans from dependency plugin contexts when injectClassLoader=true
		beanBridge.bridge(childContext, plugin);

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

				// Bridge parent events to child context, but avoid echoing events that originated
				// from this child context (they are already delivered locally via Spring).
				ApplicationListener<ApplicationEvent> bridge = event -> {
					if (event.getSource() == childContext) return;
					// Also skip if the payload was created by the plugin classloader to prevent
					// the child -> parent -> child loop that causes duplicate handling.
					if (event instanceof PayloadApplicationEvent<?> pae) {
						Object payload = pae.getPayload();
						if (payload.getClass().getClassLoader() == childContext.getClassLoader())
							return;
					}

					childMulticaster.multicastEvent(event);
				};
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
			if (cl == null) continue; // Skip system classes with null class loader

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
