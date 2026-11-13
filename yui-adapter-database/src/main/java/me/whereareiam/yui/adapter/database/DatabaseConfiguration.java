package me.whereareiam.yui.adapter.database;

import com.zaxxer.hikari.HikariDataSource;
import me.whereareiam.yui.model.config.persistence.EmbeddedPersistence;
import me.whereareiam.yui.model.config.persistence.Persistence;
import me.whereareiam.yui.model.config.persistence.Pool;
import me.whereareiam.yui.model.config.persistence.ServerPersistence;
import me.whereareiam.yui.type.DatabaseType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Path;

/**
 * Connects to the database {@code persistence.yml} chooses. {@link PersistenceConfiguration} migrates and maps it.
 */
@Configuration
public class DatabaseConfiguration {
	@Bean
	public DatabaseType databaseType(Persistence persistence) {
		return persistence.getType();
	}

	@Bean(destroyMethod = "close")
	public DataSource dataSource(Persistence persistence, @Qualifier("dataPath") Path dataPath) {
		Pool pool = persistence.getPool();
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(jdbcUrl(persistence, dataPath));
		dataSource.setMinimumIdle(pool.getSize().getMin());
		dataSource.setMaximumPoolSize(pool.getSize().getMax());
		dataSource.setConnectionTimeout(pool.getTimeouts().getConnect().getSeconds() * 1000);
		dataSource.setIdleTimeout(pool.getTimeouts().getIdle().getSeconds() * 1000);
		dataSource.setMaxLifetime(pool.getTimeouts().getLifetime().getSeconds() * 1000);

		switch (persistence) {
			// An H2 file belongs to Yui alone; its user is fixed when the file is created.
			case EmbeddedPersistence ignored -> {
				dataSource.setUsername("sa");
				dataSource.setPassword("");
			}
			case ServerPersistence server -> {
				dataSource.setUsername(server.getUsername());
				dataSource.setPassword(server.getPassword());
			}
		}

		return dataSource;
	}

	static @NotNull String jdbcUrl(@NotNull Persistence persistence, @NotNull Path dataPath) {
		return switch (persistence) {
			case EmbeddedPersistence embedded -> "jdbc:h2:file:" + dataPath.resolve(embedded.getFile()).toAbsolutePath();
			case ServerPersistence server -> "jdbc:" + scheme(server.getType()) + "://" + server.getHost() + ":"
					+ port(server) + "/" + server.getDatabase();
		};
	}

	private static String scheme(DatabaseType type) {
		return type == DatabaseType.MARIADB ? "mariadb" : "postgresql";
	}

	private static int port(ServerPersistence server) {
		if (server.getPort() != null) return server.getPort();

		return server.getType() == DatabaseType.MARIADB ? 3306 : 5432;
	}
}
