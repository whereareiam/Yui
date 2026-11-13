package me.whereareiam.yui.common.config.jackson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;

/**
 * Turns an enum constant's name into the camel case configuration files write ({@code LOCAL_BUILDS} and
 * {@code localBuilds}) and back.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CamelCase {
	static String fromConstant(String constant) {
		StringBuilder camel = new StringBuilder();
		for (String part : constant.toLowerCase(Locale.ROOT).split("_")) {
			if (camel.isEmpty()) camel.append(part);
			else camel.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}

		return camel.toString();
	}

	static String toConstant(String camel) {
		return camel.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
	}
}
