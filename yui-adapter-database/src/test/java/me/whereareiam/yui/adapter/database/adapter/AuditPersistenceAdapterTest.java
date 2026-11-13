package me.whereareiam.yui.adapter.database.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.whereareiam.yui.adapter.database.TestDatabaseConfiguration;
import me.whereareiam.yui.persistence.AuditPersistence.Record;
import me.whereareiam.yui.type.audit.AuditCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link AuditPersistenceAdapter} on a real database. Each database Yui supports runs these tests through a
 * subclass that names it.
 */
@Transactional
@SpringBootTest(classes = TestDatabaseConfiguration.class)
abstract class AuditPersistenceAdapterTest {
	private static final Instant NOON = Instant.parse("2026-11-13T12:00:00Z");

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private AuditPersistenceAdapter adapter;

	@Test
	void search_ReturnsTheNewestFirst() {
		adapter.save(new Record(AuditCategory.MODERATION, "member.kick", NOON, "First"));
		adapter.save(new Record(AuditCategory.MODERATION, "member.ban", NOON.plusSeconds(60), "Second"));
		reread();

		List<Record> found = adapter.search(null, null, 10);

		assertEquals(List.of("Second", "First"), found.stream().map(Record::content).toList());
	}

	@Test
	void search_ByCategoryAndPartOfTheEntry_KeepsOnlyThoseThatMatch() {
		adapter.save(new Record(AuditCategory.MODERATION, "member.kick", NOON, "Kicked"));
		adapter.save(new Record(AuditCategory.MEMBERS, "member.join", NOON, "Joined"));
		adapter.save(new Record(AuditCategory.MODERATION, "message.delete", NOON, "Deleted"));
		reread();

		List<Record> found = adapter.search(AuditCategory.MODERATION, "member", 10);

		assertEquals(List.of(new Record(AuditCategory.MODERATION, "member.kick", NOON, "Kicked")), found);
	}

	@Test
	void search_ReturnsNoMoreThanTheLimit() {
		for (int index = 0; index < 5; index++)
			adapter.save(new Record(AuditCategory.MEMBERS, "member.join", NOON.plusSeconds(index), "Joined " + index));
		reread();

		assertEquals(2, adapter.search(null, null, 2).size());
	}

	@Test
	void save_ALongText_IsCutToTheColumn() {
		adapter.save(new Record(AuditCategory.MEMBERS, "member.join", NOON, "x".repeat(5000)));
		reread();

		assertEquals(4000, adapter.search(null, null, 1).getFirst().content().length());
	}

	@Test
	void removeBefore_RemovesOnlyOlderEntries() {
		adapter.save(new Record(AuditCategory.MEMBERS, "member.join", NOON.minusSeconds(3600), "Old"));
		adapter.save(new Record(AuditCategory.MEMBERS, "member.join", NOON, "New"));
		reread();

		assertEquals(1, adapter.removeBefore(NOON));
		reread();

		assertEquals(List.of("New"), adapter.search(null, null, 10).stream().map(Record::content).toList());
	}

	// Writes what is pending and forgets loaded entities, so the next search reads the table.
	private void reread() {
		entityManager.flush();
		entityManager.clear();
	}
}
