package me.whereareiam.yui.adapter.database.adapter;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.persistence.PluginDatabase;
import me.whereareiam.yui.api.type.DatabaseType;
import org.flywaydb.core.Flyway;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Opens a plugin's tables over Yui's pool: Flyway with the plugin's migrations and a history table of its own, then a
 * Hibernate unit of the plugin's entities that only validates them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginDatabaseAdapter implements PluginDatabase {
	private final DataSource dataSource;
	private final DatabaseType databaseType;

	@Override
	public @NotNull EntityManagerFactory open(
			@NotNull String plugin,
			@NotNull ClassLoader loader,
			@NotNull String migrations,
			@NotNull String entities
	) {
		String prefix = plugin.toLowerCase(Locale.ROOT).replace('-', '_') + "_";
		migrate(plugin, loader, migrations, prefix);

		EntityManagerFactory factory = map(plugin, loader, entities, dataSource);
		try {
			checkNames(plugin, factory, entities, prefix);
		} catch (RuntimeException exception) {
			factory.close();
			throw exception;
		}

		return factory;
	}

	private void migrate(String plugin, ClassLoader loader, String migrations, String prefix) {
		String location = "classpath:" + migrations;
		Flyway flyway = Flyway.configure(loader)
				.dataSource(dataSource)
				.locations(location + "common", location + databaseType.name().toLowerCase(Locale.ROOT))
				.table(prefix + "schema_history")
				// Yui's tables are there before any plugin's: a plugin's history starts empty, below its V1.
				.baselineOnMigrate(true)
				.baselineVersion("0")
				.load();
		if (flyway.info().pending().length == 0 && flyway.info().applied().length == 0)
			throw new IllegalStateException("'" + plugin + "' declares @Tables, but has no migrations in " + migrations);

		int applied = flyway.migrate().migrationsExecuted;
		if (applied > 0) log.info("[Database] Applied {} migrations of {}", applied, plugin);
	}

	private static EntityManagerFactory map(String plugin, ClassLoader loader, String entities, DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean unit = new LocalContainerEntityManagerFactoryBean();
		unit.setPersistenceUnitName(plugin);
		unit.setDataSource(dataSource);
		unit.setBeanClassLoader(loader);
		unit.setResourceLoader(new DefaultResourceLoader(loader));
		unit.setPackagesToScan(entities);
		unit.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		unit.setJpaPropertyMap(Map.of(
				"hibernate.hbm2ddl.auto", "validate",
				"hibernate.classLoaders", List.of(loader)
		));

		unit.afterPropertiesSet();

		return unit.getNativeEntityManagerFactory();
	}

	// Each table carries the plugin's id, so no plugin can reach another's tables or Yui's.
	private static void checkNames(String plugin, EntityManagerFactory factory, String entities, String prefix) {
		List<String> tables = new ArrayList<>();
		factory.unwrap(SessionFactoryImplementor.class).getMappingMetamodel()
				.forEachEntityDescriptor(entity -> tables.addAll(List.of((String[]) entity.getPropertySpaces())));
		if (tables.isEmpty())
			throw new IllegalStateException("'" + plugin + "' declares @Tables, but has no entities in " + entities);

		List<String> foreign = tables.stream().filter(table -> !table.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
		if (!foreign.isEmpty())
			throw new IllegalStateException("The tables " + foreign + " of '" + plugin + "' do not start with " + prefix);
	}
}
