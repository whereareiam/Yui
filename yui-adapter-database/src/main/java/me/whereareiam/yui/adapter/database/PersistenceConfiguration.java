package me.whereareiam.yui.adapter.database;

import me.whereareiam.yui.type.DatabaseType;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Locale;
import java.util.Map;

/**
 * Brings the database's tables up to date with Flyway, then maps them with Hibernate, which only checks that they
 * match the entities and never changes them.
 */
@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class
})
public class PersistenceConfiguration {
	/**
	 * Where Yui's migrations live. Those that read the same on every database are in {@code common}, the others in a
	 * folder per database.
	 */
	static final String MIGRATIONS = "classpath:db/migration/yui/";

	/**
	 * The table in which Flyway records Yui's migrations. A plugin keeps its own.
	 */
	static final String HISTORY_TABLE = "yui_schema_history";

	@Bean(initMethod = "migrate")
	public Flyway flyway(DataSource dataSource, DatabaseType databaseType) {
		return Flyway.configure(getClass().getClassLoader())
				.dataSource(dataSource)
				.locations(MIGRATIONS + "common", MIGRATIONS + databaseType.name().toLowerCase(Locale.ROOT))
				.table(HISTORY_TABLE)
				.load();
	}

	// Takes the migrated Flyway so that Hibernate validates the tables only once they are up to date.
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Flyway flyway) {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(dataSource);
		entityManagerFactory.setPackagesToScan(getClass().getPackage().getName() + ".entity");
		entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		entityManagerFactory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "validate"));

		return entityManagerFactory;
	}

	@Bean(name = "transactionManager")
	public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());

		return transactionManager;
	}
}
