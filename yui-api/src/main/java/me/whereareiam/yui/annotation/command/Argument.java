package me.whereareiam.yui.annotation.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as a command argument, mirroring Cloud's {@code @Argument}.
 * <p>
 * The {@link #value()} should match the argument name in the command syntax.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

	/**
	 * Name of the argument, e.g. {@code "category"} for {@code <category>}.
	 */
	String value();
}

