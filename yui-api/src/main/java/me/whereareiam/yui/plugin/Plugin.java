package me.whereareiam.yui.plugin;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a plugin's main class: the Spring configuration of the plugin's context, which scans the main class's package
 * unless {@link #scan()} names others, and attaches the plugin to Yui. A main class without it fails to load.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Configuration
@ComponentScan
@Import({PluginBeans.class, PluginAttachment.class})
public @interface Plugin {
	/**
	 * @return the packages to scan; empty scans the main class's package and its subpackages
	 */
	@AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
	String[] scan() default {};
}
