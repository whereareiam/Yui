package me.whereareiam.yui.model.plugin;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public final class Dependency {
	/**
	 * Target plugin id.
	 */
	private String id;

	/**
	 * When {@code false} the dependency is optional –
	 * the plugin will still load even if it is missing.
	 * Defaults to {@code false}.
	 */
	private boolean required;

	/**
	 * If {@code true} the dependent plug‑in will receive this dependency’s
	 * class‑loader on its own search path (in addition to its own JAR and
	 * the application class‑path).
	 * Defaults to {@code false} to preserve today’s behaviour.
	 */
	private boolean injectClassLoader;
}

