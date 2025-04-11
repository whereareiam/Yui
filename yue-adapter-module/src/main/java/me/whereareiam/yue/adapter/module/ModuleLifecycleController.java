package me.whereareiam.yue.adapter.module;

import me.whereareiam.yue.api.model.module.InternalModule;
import me.whereareiam.yue.api.output.module.YueModule;
import me.whereareiam.yue.api.type.ModuleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

@Service
public class ModuleLifecycleController {
	private final Logger logger = LoggerFactory.getLogger(ModuleLifecycleController.class);
	private final ApplicationContext ctx;

	@Autowired
	public ModuleLifecycleController(ApplicationContext ctx) {
		this.ctx = ctx;
	}

	public void loadModule(InternalModule module) {
		if (!module.getState().equals(ModuleState.UNKNOWN)) return;

		try {
			URLClassLoader loader = new URLClassLoader(new URL[]{module.getPath().toUri().toURL()}, ctx.getClassLoader());
			Class<?> moduleClass = Class.forName(module.getMain(), true, loader);

			YueModule moduleInstance = (YueModule) moduleClass.getDeclaredConstructor().newInstance();
			module.setModule(moduleInstance);

			module.setState(ModuleState.LOADED);
			module.getModule().setModule(module);
			module.getModule().setWorkingPath(module.getPath().getParent().resolve(module.getName()));
			ctx.getAutowireCapableBeanFactory().autowireBean(moduleInstance);

			logger.info("Loaded module {}{}{} v{} [{}]", AnsiColor.YELLOW, module.getName(), AnsiColor.WHITE, module.getVersion(), String.join(", ", module.getAuthors()));

			module.getModule().onLoad();
		} catch (MalformedURLException | ClassNotFoundException e) {
			logger.error("Failed to load module {}: {}", module.getName(), e);
			module.setState(ModuleState.ERROR);
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
		         InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	public void enableModule(InternalModule module) {
		if (!module.getState().equals(ModuleState.LOADED)) return;

		module.setState(ModuleState.ENABLED);
		module.getModule().onEnable();
	}

	public void disableModule(InternalModule module) {
		if (!module.getState().equals(ModuleState.ENABLED)) return;

		module.setState(ModuleState.DISABLED);
		module.getModule().onDisable();
	}

	public void unloadModule(InternalModule module) {
		if (!module.getState().equals(ModuleState.DISABLED)) return;

		module.setState(ModuleState.UNLOADED);
		module.getModule().onUnload();
	}
}