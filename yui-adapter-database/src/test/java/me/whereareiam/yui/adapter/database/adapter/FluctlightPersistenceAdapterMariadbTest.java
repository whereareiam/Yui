package me.whereareiam.yui.adapter.database.adapter;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

@Testcontainers(disabledWithoutDocker = true)
class FluctlightPersistenceAdapterMariadbTest extends FluctlightPersistenceAdapterTest {
	@Container
	static MariaDBContainer mariadb = new MariaDBContainer("mariadb:11.8");

	@DynamicPropertySource
	static void database(DynamicPropertyRegistry registry) {
		registry.add("test.database.type", () -> "MARIADB");
		registry.add("test.database.url", mariadb::getJdbcUrl);
		registry.add("test.database.username", mariadb::getUsername);
		registry.add("test.database.password", mariadb::getPassword);
	}
}
