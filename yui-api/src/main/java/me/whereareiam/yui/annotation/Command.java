package me.whereareiam.yui.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Command {
	/**
	 * The name of the command. Will be used for fetching the command from the config.
	 */
	String name();
}
