package me.whereareiam.yue.api.output.plugin;

public interface YuePlugin {
	default void onLoad() {
	}

	default void onEnable() {
	}

	default void onDisable() {
	}

	default void onUnload() {
	}
}