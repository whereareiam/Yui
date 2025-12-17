package me.whereareiam.yui.adapter.database;

import com.zaxxer.hikari.HikariDataSource;
import me.whereareiam.yui.model.config.settings.Settings;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class
})
public class DatabaseConfiguration {
	@Bean
	public DataSource dataSource(Settings settings) {
		HikariDataSource dataSource = new HikariDataSource();

		String url = "jdbc:postgresql://" + settings.getDatabase().getHostname() + ":" +
				settings.getDatabase().getPort() + "/" + settings.getDatabase().getDatabase();

		dataSource.setJdbcUrl(url);
		dataSource.setUsername(settings.getDatabase().getUsername());
		dataSource.setPassword(settings.getDatabase().getPassword());

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