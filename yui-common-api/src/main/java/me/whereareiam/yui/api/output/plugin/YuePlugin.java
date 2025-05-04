package me.whereareiam.yui.api.output.plugin;

public interface YuiPlugin {
	default void onLoad() {
	}

	default void onEnable() {
	}

	default void onDisable() {
	}

	default void onUnload() {
	}
}