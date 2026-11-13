package me.whereareiam.yui.common.config.jackson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces each {@code ${NAME}} in a configuration value with the environment variable of that name, so secrets
 * such as a token stay out of the files. A variable that is not set stops the file from being read.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class EnvironmentText {
	private static final Pattern VARIABLE = Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)}");

	static String resolve(String text) {
		return resolve(text, System::getenv);
	}

	static String resolve(String text, UnaryOperator<String> environment) {
		if (!text.contains("${")) return text;

		Matcher matcher = VARIABLE.matcher(text);
		StringBuilder resolved = new StringBuilder();
		while (matcher.find()) {
			String value = environment.apply(matcher.group(1));
			if (value == null)
				throw new IllegalStateException("The environment variable " + matcher.group(1) + " is not set");

			matcher.appendReplacement(resolved, Matcher.quoteReplacement(value));
		}
		matcher.appendTail(resolved);

		return resolved.toString();
	}
}
