package me.whereareiam.yui.example.model.message;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code messages/<locale>/commands.yml}: the texts of {@code /hello}, as {@code greeting.commands.hello.*}.
 */
@Getter
@Setter
public class CommandMessages {
	private Hello hello;

	@Getter
	@Setter
	public static class Hello {
		private String description;
		private String example;

		/**
		 * The greeting; {@code {mention}} is who ran the command, {@code {emoji}} the one of {@code settings.yml}.
		 */
		private String reply;
	}
}
