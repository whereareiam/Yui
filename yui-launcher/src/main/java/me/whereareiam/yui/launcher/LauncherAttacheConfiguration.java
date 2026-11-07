package me.whereareiam.yui.launcher;

import me.whereareiam.attache.type.VerbosityMode;

import java.util.function.Consumer;

final class BootstrapAttacheConfiguration {
	private String libraryPath = ".libraries";
	private VerbosityMode verbosityMode = VerbosityMode.SUMMARY;

	static BootstrapAttacheConfiguration load() {
		BootstrapAttacheConfiguration configuration = new BootstrapAttacheConfiguration();
		configuration.applySystemProperties();
		return configuration;
	}

	String getLibraryPath() {
		return libraryPath;
	}

	VerbosityMode getVerbosityMode() {
		return verbosityMode;
	}

	private void applySystemProperties() {
		applySystemProperty("attache.library-path", value -> libraryPath = value);
		applySystemProperty("attache.verbosity-mode", value -> verbosityMode = parseVerbosityMode(value));
	}

	private void applySystemProperty(String key, Consumer<String> consumer) {
		String value = System.getProperty(key);
		if (value == null || value.isBlank())
			return;

		consumer.accept(value.trim());
	}

	private VerbosityMode parseVerbosityMode(String value) {
		return VerbosityMode.valueOf(value.trim().replace('-', '_').toUpperCase());
	}
}
