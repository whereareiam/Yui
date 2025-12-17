package me.whereareiam.yui.model.plugin;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.plugin.YuiPlugin;
import me.whereareiam.yui.type.PluginState;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Getter
public final class InternalPlugin {
	private final Plugin descriptor;
	private final ClassLoader classLoader;
	private final AnnotationConfigApplicationContext context;
	private final YuiPlugin instance;
	@Setter
	private PluginState state = PluginState.LOADED;

	public InternalPlugin(
			Plugin descriptor,
			ClassLoader classLoader,
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

	public boolean isEnabled() {
		return state == PluginState.ENABLED;
	}
}