package me.whereareiam.yui.common.adapter;

import me.whereareiam.configura.TypeAdapter;
import me.whereareiam.yui.model.type.Duration;

/**
 * Configura adapter for the Duration type.
 * <p>
 * Allows Duration objects to be serialized and deserialized in configuration files
 * using human-readable format (e.g., "5m", "1h30m", "2d").
 */
public class DurationAdapter implements TypeAdapter<Duration> {
	@Override
	public Duration deserialize(String value) {
		return Duration.parse(value);
	}

	@Override
	public String serialize(Duration value) {
		return value.toString();
	}
}
