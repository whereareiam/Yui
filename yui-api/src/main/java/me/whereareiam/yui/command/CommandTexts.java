package me.whereareiam.yui.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * The keys of one command's texts in {@code messages/<locale>/commands.yml}: its description, its example and the
 * description of each argument. A plugin's command has them under the plugin's id, as all its texts.
 *
 * <pre>{@code
 * CommandTexts.of("internal", "update-check").description();   // commands.updateCheck.description
 * CommandTexts.of("verification", "verify").variable("user");  // verification.commands.verify.variables.user
 * }</pre>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandTexts {
	/**
	 * The owner of Yui's own commands.
	 */
	public static final String CORE = "internal";

	private final String prefix;

	/**
	 * @param owner the provider of the command's definition: {@link #CORE} or a plugin's id
	 * @param id    the command's id in {@code commands.yml}, such as {@code update-check}
	 * @return the keys of its texts
	 */
	public static @NotNull CommandTexts of(@NotNull String owner, @NotNull String id) {
		String owned = owner.equals(CORE) ? "" : owner.toLowerCase(Locale.ROOT) + ".";

		return new CommandTexts(owned + "commands." + camel(id) + ".");
	}

	public @NotNull String description() {
		return prefix + "description";
	}

	public @NotNull String example() {
		return prefix + "example";
	}

	/**
	 * @param argument an argument's name in the command's usage
	 * @return the key of its description
	 */
	public @NotNull String variable(@NotNull String argument) {
		return prefix + "variables." + argument;
	}

	// The texts' models write keys in camel case: update-check is updateCheck.
	private static String camel(String id) {
		StringBuilder camel = new StringBuilder();
		boolean upper = false;
		for (char character : id.toCharArray()) {
			if (character == '-' || character == '_') {
				upper = true;
				continue;
			}

			camel.append(upper ? Character.toUpperCase(character) : character);
			upper = false;
		}

		return camel.toString();
	}
}
