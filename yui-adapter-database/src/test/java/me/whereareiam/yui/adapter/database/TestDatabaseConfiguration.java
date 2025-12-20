package me.whereareiam.yui.adapter.database;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test configuration for database tests using Testcontainers.
 * Provides Spring Boot configuration for @DataJpaTest.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories
public class TestDatabaseConfiguration {
}

