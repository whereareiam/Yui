package me.whereareiam.yui.annotation.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a default value for an optional argument, similar to Cloud's {@code @Default}.
 * <p>
 * The value is stored as a string and interpreted by the command framework
 * according to the target parameter type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Default {

	/**
	 * Default value to use when the argument is omitted.
	 */
	String value();
}