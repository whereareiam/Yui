package me.whereareiam.yui.adapter.database.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.whereareiam.yui.adapter.database.TestDatabaseConfiguration;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.api.persistence.FluctlightPersistence.Facts;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link FluctlightPersistenceAdapter} on a real database, with the tables Yui's migrations create. Each
 * database Yui supports runs these tests through a subclass that names it.
 */
@Transactional
@SpringBootTest(classes = TestDatabaseConfiguration.class)
abstract class FluctlightPersistenceAdapterTest {
	private static final long USER = 123L;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private FluctlightPersistenceAdapter adapter;

	@Autowired
	private Flyway flyway;

	@BeforeEach
	void setUp() {
		for (DiscordLocale locale : Set.of(DiscordLocale.ENGLISH_US, DiscordLocale.GERMAN, DiscordLocale.FRENCH))
			entityManager.persist(LanguageEntity.builder().locale(locale).build());

		entityManager.flush();
	}

	@Test
	void migrations_AreAppliedAndRecordedInYuisHistoryTable() {
		assertEquals("yui_schema_history", flyway.getConfiguration().getTable());
		assertTrue(flyway.info().applied().length >= 1);
		assertEquals(0, flyway.info().pending().length);
	}

	@Test
	void load_WhenNothingIsStored_ReturnsEmpty() {
		assertTrue(adapter.load(USER).isEmpty());
	}

	@Test
	void save_ThenLoad_ReturnsTheFacts() {
		Facts facts = new Facts(DiscordLocale.ENGLISH_US, Set.of(DiscordLocale.GERMAN, DiscordLocale.FRENCH));

		adapter.save(USER, facts);
		reread();

		assertEquals(facts, adapter.load(USER).orElseThrow());
	}

	@Test
	void save_ReplacesEarlierFacts() {
		adapter.save(USER, new Facts(DiscordLocale.ENGLISH_US, Set.of(DiscordLocale.GERMAN)));
		reread();

		Facts replaced = new Facts(DiscordLocale.FRENCH, Set.of());
		adapter.save(USER, replaced);
		reread();

		assertEquals(replaced, adapter.load(USER).orElseThrow());
	}

	@Test
	void save_WithoutPrimaryLanguage_StoresNone() {
		adapter.save(USER, new Facts(null, Set.of()));
		reread();

		assertEquals(new Facts(null, Set.of()), adapter.load(USER).orElseThrow());
	}

	@Test
	void save_WithALanguageYuiDoesNotOffer_Fails() {
		Facts facts = new Facts(DiscordLocale.JAPANESE, Set.of());

		assertThrows(IllegalArgumentException.class, () -> adapter.save(USER, facts));
	}

	// Writes what is pending and forgets loaded entities, so the next load reads the tables.
	private void reread() {
		entityManager.flush();
		entityManager.clear();
	}
}
