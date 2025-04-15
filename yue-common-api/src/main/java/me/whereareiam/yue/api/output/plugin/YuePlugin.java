package me.whereareiam.yue.api.output.plugin;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Base class for all Yue plugins.
 * Provides the lifecycle methods and core functionality for plugin management.
 *
 * <p>YuePluginDescriptor lifecycle:</p>
 * <ol>
 *   <li>{@link #start()} - Invoked when the plugin is started.</li>
 *   <li>{@link #stop()} - Invoked when the plugin is stopped.</li>
 *   <li>{@link #delete()} - Invoked when the plugin is deleted.</li>
 * </ol>
 */
public class YuePlugin extends SpringPlugin {
	public YuePlugin(PluginWrapper wrapper) {
		super(wrapper);

		createApplicationContext();
	}

	@Override
	public ApplicationContext createApplicationContext() {
		ApplicationContext parentContext = ((SpringPluginManager) getWrapper().getPluginManager()).getApplicationContext();

		// Create and configure the context
		AnnotationConfigApplicationContext childContext = new AnnotationConfigApplicationContext();
		childContext.setClassLoader(getWrapper().getPluginClassLoader());
		childContext.setParent(parentContext);

		// Register plugin beans
		childContext.registerBean(PluginWrapper.class, this::getWrapper);

		childContext.scan(getClass().getPackage().getName());
		childContext.refresh();

		this.applicationContext = childContext;

		return childContext;
	}

}