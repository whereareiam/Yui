package me.whereareiam.yui.adapter.database;

import me.whereareiam.yui.model.config.persistence.EmbeddedPersistence;
import me.whereareiam.yui.model.config.persistence.ServerPersistence;
import me.whereareiam.yui.type.DatabaseType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConfigurationTest {
	@Test
	void jdbcUrl_ForH2_PointsAtTheFileInTheDataFolder() {
		EmbeddedPersistence embedded = new EmbeddedPersistence();
		embedded.setType(DatabaseType.H2);
		embedded.setFile("database/yui");

		assertEquals("jdbc:h2:file:/srv/yui/database/yui", DatabaseConfiguration.jdbcUrl(embedded, Path.of("/srv/yui")));
	}

	@Test
	void jdbcUrl_ForPostgresql_NamesTheServer() {
		assertEquals("jdbc:postgresql://db.local:5433/yui",
				DatabaseConfiguration.jdbcUrl(server(DatabaseType.POSTGRESQL, 5433), Path.of("/srv/yui")));
	}

	@Test
	void jdbcUrl_ForMariadb_NamesTheServer() {
		assertEquals("jdbc:mariadb://db.local:3307/yui",
				DatabaseConfiguration.jdbcUrl(server(DatabaseType.MARIADB, 3307), Path.of("/srv/yui")));
	}

	@Test
	void jdbcUrl_WithoutPort_TakesTheTypesOwn() {
		assertEquals("jdbc:postgresql://db.local:5432/yui",
				DatabaseConfiguration.jdbcUrl(server(DatabaseType.POSTGRESQL, null), Path.of("/srv/yui")));
		assertEquals("jdbc:mariadb://db.local:3306/yui",
				DatabaseConfiguration.jdbcUrl(server(DatabaseType.MARIADB, null), Path.of("/srv/yui")));
	}

	private static ServerPersistence server(DatabaseType type, Integer port) {
		ServerPersistence server = new ServerPersistence();
		server.setType(type);
		server.setHost("db.local");
		server.setPort(port);
		server.setDatabase("yui");

		return server;
	}
}
