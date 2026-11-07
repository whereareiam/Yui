package me.whereareiam.yui.launcher;

import me.whereareiam.attache.common.logging.adapter.JDKLoggingHelper;
import me.whereareiam.attache.platform.standalone.StandaloneLibraryManager;
import me.whereareiam.yui.YuiApplication;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.logging.Logger;

public final class YuiLauncher {
	private static volatile StandaloneLibraryManager libraryManager;

	static void main(String[] args) {
		preloadRuntimeDependencies();
		YuiApplication.main(args);
	}

	private static void preloadRuntimeDependencies() {
		if (libraryManager != null) return;

		BootstrapAttacheConfiguration configuration = BootstrapAttacheConfiguration.load();
		StandaloneLibraryManager manager = new StandaloneLibraryManager(
				new JDKLoggingHelper(Logger.getLogger("Attache")),
				Paths.get(configuration.getLibraryPath()),
				".",
				YuiLauncher.class.getClassLoader(),
				false
		);
		manager.setVerbosityMode(configuration.getVerbosityMode());
		manager.loadClasspathDescriptors();
		libraryManager = manager;
	}

	public static @Nullable StandaloneLibraryManager getLibraryManager() {
		return libraryManager;
	}

	public static void setLibraryManager(StandaloneLibraryManager libraryManager) {
		YuiLauncher.libraryManager = libraryManager;
	}
}
