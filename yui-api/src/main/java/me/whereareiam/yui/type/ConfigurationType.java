package me.whereareiam.yui.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfigurationType {
	/**
	 * YAML configuration format with .yml extension
	 */
	YAML(".yml"),

	/**
	 * JSON configuration format with .json extension
	 */
	JSON(".json");

	private final String extension;
}