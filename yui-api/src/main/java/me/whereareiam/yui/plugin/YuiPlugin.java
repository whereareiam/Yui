package me.whereareiam.yui.plugin;

import me.whereareiam.attache.model.LibraryRequest;

import java.util.List;

public interface YuiPlugin {
	default void onLoad() {
	}

	default void onEnable() {
	}

	default void onDisable() {
	}

	default void onUnload() {
	}

	default List<String> repositories() {
		return List.of();
	}

	default List<LibraryRequest> dependencies() {
		return List.of();
	}
}
