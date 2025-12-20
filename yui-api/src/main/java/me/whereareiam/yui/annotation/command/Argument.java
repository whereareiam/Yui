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
 * If left empty, the parameter name will be used (when available), similar to Cloud.
 * <p>
 * Additional optional attributes are provided to closely replicate Cloud:
 * <ul>
 *     <li>{@link #parser()} - named parser to use, if any</li>
 *     <li>{@link #suggestions()} - named suggestion provider to use, if any</li>
 *     <li>{@link #description()} - human-readable argument description</li>
 * </ul>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

	/**
	 * Name of the argument, e.g. {@code "category"} for {@code <category>}.
	 * <p>
	 * If left empty, the argument name will be inferred from the parameter name
	 * when available.
	 */
	String value() default "";

	/**
	 * Name of the argument parser to use. If empty, the default parser for the
	 * parameter type will be used.
	 */
	String parser() default "";

	/**
	 * Name of the suggestion provider to use. If empty, the default provider
	 * for the argument parser will be used.
	 */
	String suggestions() default "";

	/**
	 * Human-readable description of the argument.
	 */
	String description() default "";
}

