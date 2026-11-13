package me.whereareiam.yui.api.exception;

public class ConfigLoadException extends RuntimeException {
	/**
	 * Constructs a new ConfigLoadException with the specified detail message and cause.
	 *
	 * @param message the detail message describing the error
	 * @param cause   the cause of the configuration loading failure
	 */
	public ConfigLoadException(String message, Throwable cause) {
		super(message, cause);
	}
}
