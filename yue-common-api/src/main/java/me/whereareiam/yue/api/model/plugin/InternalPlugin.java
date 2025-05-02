package me.whereareiam.yue.api.model.plugin;

import me.whereareiam.yue.api.output.plugin.YuePlugin;
import me.whereareiam.yue.api.type.PluginState;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.URLClassLoader;

public final class InternalPlugin {
	private final Plugin descriptor;
	private final URLClassLoader classLoader;
	private final AnnotationConfigApplicationContext context;
	private final YuePlugin instance;
	private PluginState state = PluginState.LOADED;

	public InternalPlugin(
			Plugin descriptor,
			URLClassLoader classLoader,
			AnnotationConfigApplicationContext context,
			YuePlugin instance
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

	public YuePlugin getYuePlugin() {
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