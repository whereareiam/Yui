package me.whereareiam.yui.api.model.config.persistence;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.model.type.Duration;

/**
 * The connections Yui keeps to its database: how many, and how long they wait and live.
 */
@Getter
@Setter
public class Pool {
	private Size size;
	private Timeouts timeouts;

	/**
	 * The fewest connections kept open, and the most opened at once.
	 */
	@Getter
	@Setter
	public static class Size {
		private int min;
		private int max;
	}

	/**
	 * How long a request waits for a connection, how long a spare connection stays idle, and how long any
	 * connection lives.
	 */
	@Getter
	@Setter
	public static class Timeouts {
		private Duration connect;
		private Duration idle;
		private Duration lifetime;
	}
}
