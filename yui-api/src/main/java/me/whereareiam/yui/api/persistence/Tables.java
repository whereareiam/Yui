package me.whereareiam.yui.api.persistence;

import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gives a plugin tables of its own in Yui's database, beside {@link me.whereareiam.yui.api.plugin.Plugin} on its main
 * class. Before the plugin starts, Flyway brings them up to date from the plugin's migrations, with a history table
 * of the plugin's own ({@code onboarding_schema_history}); Hibernate then maps the plugin's entities and only checks
 * that they match. The plugin's Spring Data repositories and {@code @Transactional} methods work on them.
 *
 * <pre>{@code
 * @Plugin
 * @Tables
 * public class Onboarding implements YuiPlugin {
 * }
 * }</pre>
 *
 * <p>Migrations live in {@code db/migration/<plugin id>/}, in {@code common/} when they read the same on every
 * database and in {@code h2/}, {@code postgresql/} or {@code mariadb/} otherwise; entities in the main class's package
 * {@code .entity}. Each table's name starts with the plugin's id ({@code onboarding_members}). A plugin without
 * migrations or entities, or with a table of another name, does not start.</p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(PluginTables.class)
@EnableTransactionManagement
@EnableJpaRepositories(
		entityManagerFactoryRef = PluginTables.ENTITY_MANAGER_FACTORY,
		transactionManagerRef = PluginTables.TRANSACTION_MANAGER
)
public @interface Tables {
	/**
	 * @return the folder of the migrations on the plugin's class path; empty is {@code db/migration/<plugin id>/}
	 */
	String migrations() default "";

	/**
	 * @return the package of the entities; empty is the main class's package {@code .entity}
	 */
	String entities() default "";
}
