package me.whereareiam.yui.model.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a duration of time that can be parsed from human-readable strings.
 * <p>
 * Supports the following formats:
 * <ul>
 *   <li>Seconds: {@code 30s}, {@code 600s}</li>
 *   <li>Minutes: {@code 5m}, {@code 30m}</li>
 *   <li>Hours: {@code 2h}, {@code 24h}</li>
 *   <li>Days: {@code 1d}, {@code 7d}</li>
 *   <li>Weeks: {@code 1w}, {@code 2w}</li>
 *   <li>Combined: {@code 1h30m}, {@code 2d12h}, {@code 1w3d}</li>
 * </ul>
 * <p>
 * Examples:
 * <pre>{@code
 * Duration.parse("5m");         // 5 minutes
 * Duration.parse("1h30m");      // 1 hour and 30 minutes
 * Duration.parse("2d");         // 2 days
 * Duration.parse("1w3d12h");    // 1 week, 3 days, and 12 hours
 * }</pre>
 */
@Getter
@EqualsAndHashCode
@SuppressWarnings("unused")
public class Duration {
	private static final Pattern PATTERN = Pattern.compile("(\\d+)([smhdw])");

	private final long seconds;

	private Duration(long seconds) {
		this.seconds = seconds;
	}

	/**
	 * Parses a duration string into a Duration object.
	 *
	 * @param input The duration string (e.g., "5m", "1h30m", "2d")
	 * @return A Duration object representing the parsed time
	 * @throws IllegalArgumentException if the input format is invalid
	 */
	public static Duration parse(String input) {
		if (input == null || input.isBlank())
			throw new IllegalArgumentException("Duration string cannot be null or empty");

		String normalized = input.trim().toLowerCase();
		Matcher matcher = PATTERN.matcher(normalized);

		long totalSeconds = 0;
		boolean matched = false;

		while (matcher.find()) {
			matched = true;
			long value = Long.parseLong(matcher.group(1));
			String unit = matcher.group(2);

			totalSeconds += switch (unit) {
				case "s" -> value;
				case "m" -> value * 60;
				case "h" -> value * 3600;
				case "d" -> value * 86400;
				case "w" -> value * 604800;
				default -> throw new IllegalArgumentException("Unknown time unit: " + unit);
			};
		}

		if (!matched)
			throw new IllegalArgumentException("Invalid duration format: " + input + ". Expected format like '5m', '1h30m', '2d'");

		return new Duration(totalSeconds);
	}

	/**
	 * Creates a Duration from seconds.
	 *
	 * @param seconds The number of seconds
	 * @return A Duration object
	 */
	public static Duration ofSeconds(long seconds) {
		return new Duration(seconds);
	}

	/**
	 * Creates a Duration from minutes.
	 *
	 * @param minutes The number of minutes
	 * @return A Duration object
	 */
	public static Duration ofMinutes(long minutes) {
		return new Duration(minutes * 60);
	}

	/**
	 * Creates a Duration from hours.
	 *
	 * @param hours The number of hours
	 * @return A Duration object
	 */
	public static Duration ofHours(long hours) {
		return new Duration(hours * 3600);
	}

	/**
	 * Creates a Duration from days.
	 *
	 * @param days The number of days
	 * @return A Duration object
	 */
	public static Duration ofDays(long days) {
		return new Duration(days * 86400);
	}

	/**
	 * Gets the duration in minutes.
	 *
	 * @return The duration in minutes
	 */
	public long toMinutes() {
		return seconds / 60;
	}

	/**
	 * Gets the duration in hours.
	 *
	 * @return The duration in hours
	 */
	public long toHours() {
		return seconds / 3600;
	}

	/**
	 * Gets the duration in days.
	 *
	 * @return The duration in days
	 */
	public long toDays() {
		return seconds / 86400;
	}

	/**
	 * Converts to a TimeUnit-compatible value.
	 *
	 * @param unit The target TimeUnit
	 * @return The duration in the specified unit
	 */
	public long to(TimeUnit unit) {
		return unit.convert(seconds, TimeUnit.SECONDS);
	}

	/**
	 * Returns a human-readable string representation.
	 * <p>
	 * Always returns the most compact representation (e.g., "5m" instead of "300s").
	 *
	 * @return A string representation (e.g., "5m", "1h30m", "2d")
	 */
	@Override
	public String toString() {
		if (seconds == 0) return "0s";

		long remaining = seconds;
		StringBuilder sb = new StringBuilder();

		long weeks = remaining / 604800;
		if (weeks > 0) {
			sb.append(weeks).append("w");
			remaining %= 604800;
		}

		long days = remaining / 86400;
		if (days > 0) {
			sb.append(days).append("d");
			remaining %= 86400;
		}

		long hours = remaining / 3600;
		if (hours > 0) {
			sb.append(hours).append("h");
			remaining %= 3600;
		}

		long minutes = remaining / 60;
		if (minutes > 0) {
			sb.append(minutes).append("m");
			remaining %= 60;
		}

		if (remaining > 0 || sb.isEmpty()) {
			sb.append(remaining).append("s");
		}

		return sb.toString();
	}
}
