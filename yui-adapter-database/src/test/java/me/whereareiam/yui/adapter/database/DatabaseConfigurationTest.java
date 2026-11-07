package me.whereareiam.yui.adapter.database;

import me.whereareiam.yui.model.config.settings.database.DatabaseSettings;
import me.whereareiam.yui.type.DatabaseType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConfigurationTest {
	@Test
	void jdbcUrl_ForH2_PointsAtTheFileInTheDataFolder() {
		DatabaseSettings database = settings(DatabaseType.H2);
		database.setFile("database/yui");

		assertEquals("jdbc:h2:file:/srv/yui/database/yui", DatabaseConfiguration.jdbcUrl(database, Path.of("/srv/yui")));
	}

	@Test
	void jdbcUrl_ForPostgresql_NamesTheServer() {
		assertEquals("jdbc:postgresql://db.local:5432/yui",
				DatabaseConfiguration.jdbcUrl(settings(DatabaseType.POSTGRESQL), Path.of("/srv/yui")));
	}

	@Test
	void jdbcUrl_ForMariadb_NamesTheServer() {
		assertEquals("jdbc:mariadb://db.local:5432/yui",
				DatabaseConfiguration.jdbcUrl(settings(DatabaseType.MARIADB), Path.of("/srv/yui")));
	}

	private static DatabaseSettings settings(DatabaseType type) {
		DatabaseSettings database = new DatabaseSettings();
		database.setType(type);
		database.setHostname("db.local");
		database.setPort(5432);
		database.setDatabase("yui");

		return database;
	}
}
