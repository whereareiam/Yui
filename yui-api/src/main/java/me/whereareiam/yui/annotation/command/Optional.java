package me.whereareiam.yui.annotation.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a command parameter as optional, mirroring Cloud's {@code @Optional}.
 * <p>
 * This is especially useful when the Java type alone does not clearly express
 * whether an argument is required (for example, non-nullable primitives).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Optional {
}