package me.whereareiam.yui.annotation.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Associates a command handler (class or method) with a configuration-backed
 * definition entry, similar to {@code me.whereareiam.commandant.annotation.Definition}.
 * <p>
 * The {@link #value()} must match the key in the configuration (e.g. {@code "help"},
 * {@code "reload"}). When present both on the class and on a method, the method-level
 * annotation should take precedence.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Definition {

	/**
	 * Identifier of the definition inside the configuration, e.g. {@code "main"}.
	 */
	String value();
}

