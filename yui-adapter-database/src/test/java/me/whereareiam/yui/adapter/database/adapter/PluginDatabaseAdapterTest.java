package me.whereareiam.yui.adapter.database.adapter;

import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.chassis.model.ExtensionDescriptor;
import me.whereareiam.chassis.model.Version;
import me.whereareiam.yui.adapter.database.TestDatabaseConfiguration;
import org.example.tables.GreetingTables;
import org.example.tables.entity.WordEntity;
import org.example.tables.repository.WordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A plugin's tables, opened the way {@code @Tables} opens them: a plugin context beneath Yui's, with its own
 * migrations, history table, entities and repositories over Yui's database.
 */
@SpringBootTest(classes = TestDatabaseConfiguration.class)
@TestPropertySource(properties = {
		"test.database.type=H2",
		"test.database.url=jdbc:h2:mem:plugins;DB_CLOSE_DELAY=-1",
		"test.database.username=sa",
		"test.database.password="
})
class PluginDatabaseAdapterTest {
	@Autowired
	private ApplicationContext yui;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private PluginDatabaseAdapter adapter;

	@Test
	void aPluginWithTables_MigratesItsOwnAndUsesThemThroughItsRepositories() {
		try (AnnotationConfigApplicationContext plugin = plugin("greeting")) {
			WordRepository words = plugin.getBean(WordRepository.class);
			words.save(new WordEntity(1L, "Hello"));

			assertEquals("Hello", words.findById(1L).orElseThrow().getWord());
			assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM \"greeting_schema_history\" WHERE \"version\" = '1' AND \"success\"", Integer.class));
		}
	}

	@Test
	void aPluginWithoutMigrations_DoesNotOpen() {
		ClassLoader loader = getClass().getClassLoader();

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> adapter.open("silent", loader, "db/migration/silent/", "org.example.tables.entity"));
		assertTrue(failure.getMessage().contains("no migrations"));
	}

	private AnnotationConfigApplicationContext plugin(String id) {
		ExtensionContext extension = mock(ExtensionContext.class);
		ExtensionDescriptor descriptor = ExtensionDescriptor.builder()
				.id(id)
				.name(id)
				.version(Version.of(1, 0, 0))
				.main(GreetingTables.class.getName())
				.authors(List.of())
				.requires(Map.of())
				.build();
		when(extension.descriptor()).thenReturn(descriptor);

		AnnotationConfigApplicationContext plugin = new AnnotationConfigApplicationContext();
		plugin.setParent(yui);
		plugin.getBeanFactory().registerSingleton("extension", extension);
		plugin.register(GreetingTables.class);
		plugin.refresh();

		return plugin;
	}
}
