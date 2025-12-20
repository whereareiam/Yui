package me.whereareiam.yui.adapter.database;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;

/**
 * Test configuration for database tests using Testcontainers.
 * Provides Spring Boot configuration for @DataJpaTest.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class
})
@EnableJpaRepositories
@ComponentScan(
		basePackages = "me.whereareiam.yui.adapter.database",
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DatabaseConfiguration.class)
)
public class TestDatabaseConfiguration {
	@Bean
	public DataSource dataSource(Environment environment) {
		// Properties will be set by @DynamicPropertySource in the test
		String url = environment.getProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/test");
		String username = environment.getProperty("spring.datasource.username", "test");
		String password = environment.getProperty("spring.datasource.password", "test");
		
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setMaximumPoolSize(10);
		dataSource.setMinimumIdle(2);
		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		em.setPackagesToScan(getClass().getPackage().getName() + ".entity");

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);

		em.setJpaVendorAdapter(vendorAdapter);

		return em;
	}

	@Bean(name = "transactionManager")
	public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory.getObject());
		return txManager;
	}
}

