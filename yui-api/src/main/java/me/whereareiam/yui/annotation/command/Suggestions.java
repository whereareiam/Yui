package me.whereareiam.yui.annotation.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Associates a parameter with a named suggestion provider, mirroring Cloud's {@code @Suggestions}.
 * <p>
 * The value refers to a method or key that will be resolved by the command framework.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Suggestions {

	/**
	 * Identifier of the suggestion provider.
	 */
	String value();
}

