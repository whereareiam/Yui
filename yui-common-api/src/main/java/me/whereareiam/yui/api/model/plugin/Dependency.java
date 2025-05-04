package me.whereareiam.yui.api.model.plugin;

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
}

