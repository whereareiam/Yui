package me.whereareiam.yui.example.model;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code plugins/greeting/settings.yml}.
 *
 * <pre>{@code
 * emoji: "👋"
 * }</pre>
 */
@Getter
@Setter
public class GreetingSettings {
	/**
	 * Shown after every greeting.
	 */
	private String emoji = "👋";
}
