package me.whereareiam.yui.adapter.database;

import com.zaxxer.hikari.HikariDataSource;
import me.whereareiam.yui.api.type.DatabaseType;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Stands in for {@link DatabaseConfiguration}: connects to the database a test names through the {@code test.database.*}
 * properties, and keeps everything else of Yui's persistence, its migrations and validation included.
 */
@SpringBootConfiguration
@ComponentScan(
		basePackages = "me.whereareiam.yui.adapter.database",
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DatabaseConfiguration.class)
)
public class TestDatabaseConfiguration {
	@Bean
	public DatabaseType databaseType(Environment environment) {
		return DatabaseType.valueOf(environment.getRequiredProperty("test.database.type"));
	}

	@Bean(destroyMethod = "close")
	public DataSource dataSource(Environment environment) {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(environment.getRequiredProperty("test.database.url"));
		dataSource.setUsername(environment.getRequiredProperty("test.database.username"));
		dataSource.setPassword(environment.getRequiredProperty("test.database.password"));
		dataSource.setMaximumPoolSize(4);

		return dataSource;
	}
}
