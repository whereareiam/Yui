package me.whereareiam.yui.adapter.database;

import com.zaxxer.hikari.HikariDataSource;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.config.settings.database.DatabaseSettings;
import me.whereareiam.yui.model.config.settings.database.HikariSettings;
import me.whereareiam.yui.type.DatabaseType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Path;

/**
 * Connects to the database the settings choose. {@link PersistenceConfiguration} migrates and maps it.
 */
@Configuration
public class DatabaseConfiguration {
	@Bean
	public DatabaseType databaseType(Settings settings) {
		return settings.getDatabase().getType();
	}

	@Bean(destroyMethod = "close")
	public DataSource dataSource(Settings settings, @Qualifier("dataPath") Path dataPath) {
		DatabaseSettings database = settings.getDatabase();
		HikariSettings hikari = database.getHikari();

		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(jdbcUrl(database, dataPath));
		dataSource.setMinimumIdle(hikari.getMinimumIdle());
		dataSource.setMaximumPoolSize(hikari.getMaximumPoolSize());
		dataSource.setConnectionTimeout(hikari.getConnectionTimeout());
		dataSource.setIdleTimeout(hikari.getIdleTimeout());
		dataSource.setMaxLifetime(hikari.getMaxLifetime());

		// An H2 file belongs to Yui alone; its user is fixed when the file is created.
		if (database.getType() == DatabaseType.H2) {
			dataSource.setUsername("sa");
			dataSource.setPassword("");
		} else {
			dataSource.setUsername(database.getUsername());
			dataSource.setPassword(database.getPassword());
		}

		return dataSource;
	}

	static @NotNull String jdbcUrl(@NotNull DatabaseSettings database, @NotNull Path dataPath) {
		return switch (database.getType()) {
			case H2 -> "jdbc:h2:file:" + dataPath.resolve(database.getFile()).toAbsolutePath();
			case POSTGRESQL -> "jdbc:postgresql://" + database.getHostname() + ":" + database.getPort() + "/"
					+ database.getDatabase();
			case MARIADB -> "jdbc:mariadb://" + database.getHostname() + ":" + database.getPort() + "/"
					+ database.getDatabase();
		};
	}
}
