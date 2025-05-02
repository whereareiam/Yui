package me.whereareiam.yue.api.model.plugin;

import me.whereareiam.yue.api.output.plugin.YuePlugin;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.URLClassLoader;

public final class InternalPlugin {
	private final Plugin descriptor;
	private final URLClassLoader classLoader;
	private final AnnotationConfigApplicationContext context;
	private final YuePlugin instance;
	private boolean enabled;

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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}