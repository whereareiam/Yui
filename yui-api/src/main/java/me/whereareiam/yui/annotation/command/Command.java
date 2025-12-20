package me.whereareiam.yui.annotation.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a command and its syntax, mirroring Incendo Cloud's {@code @CommandDefinition} annotation.
 * <p>
 * Example:
 * <pre>{@code
 * @Command("main help [category]")
 * public void help(@Argument("category") String category) { ... }
 * }</pre>
 *
 * <p>
 * This annotation is part of Yui's public API so plugins can depend on it
 * without depending directly on Cloud. Runtime processing is provided by
 * Yui's command integration layer.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

	/**
	 * CommandDefinition syntax, for example: {@code "main help [category]"}.
	 */
	String value();
}

