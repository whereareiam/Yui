package me.whereareiam.yui.api.model.plugin;

import me.whereareiam.yui.api.output.plugin.YuiPlugin;
import me.whereareiam.yui.api.type.PluginState;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.URLClassLoader;

public final class InternalPlugin {
	private final Plugin descriptor;
	private final URLClassLoader classLoader;
	private final AnnotationConfigApplicationContext context;
	private final YuiPlugin instance;
	private PluginState state = PluginState.LOADED;

	public InternalPlugin(
			Plugin descriptor,
			URLClassLoader classLoader,
			AnnotationConfigApplicationContext context,
			YuiPlugin instance
	) {
		this.descriptor = descriptor;
		this.classLoader = classLoader;
		this.context = context;
		this.instance = instance;
	}

	public Plugin getPlugin() {
		return descriptor;
	}

	public URLClassLoader getClassLoader() {
		return classLoader;
	}

	public AnnotationConfigApplicationContext getContext() {
		return context;
	}

	public YuiPlugin getYuiPlugin() {
		return instance;
	}

	public PluginState getState() {
		return state;
	}

	public void setState(PluginState state) {
		this.state = state;
	}

	public boolean isEnabled() {
		return state == PluginState.ENABLED;
	}
}