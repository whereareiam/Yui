package me.whereareiam.yui.annotation.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a numeric range constraint for an argument, similar to Cloud's {@code @Range}.
 * <p>
 * This is a purely declarative annotation – validation and error reporting are
 * handled by the command framework that processes it.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Range {
	/**
	 * Inclusive minimum value. If not set, the framework should treat it as unbounded.
	 */
	double min() default Double.NEGATIVE_INFINITY;

	/**
	 * Inclusive maximum value. If not set, the framework should treat it as unbounded.
	 */
	double max() default Double.POSITIVE_INFINITY;
}