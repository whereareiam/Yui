package me.whereareiam.yui.adapter.database.adapter;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class FluctlightPersistenceAdapterPostgresqlTest extends FluctlightPersistenceAdapterTest {
	@Container
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.1");

	@DynamicPropertySource
	static void database(DynamicPropertyRegistry registry) {
		registry.add("test.database.type", () -> "POSTGRESQL");
		registry.add("test.database.url", postgres::getJdbcUrl);
		registry.add("test.database.username", postgres::getUsername);
		registry.add("test.database.password", postgres::getPassword);
	}
}
