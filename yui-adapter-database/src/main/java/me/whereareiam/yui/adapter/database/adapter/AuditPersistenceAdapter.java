package me.whereareiam.yui.adapter.database.adapter;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.adapter.database.entity.AuditEntryEntity;
import me.whereareiam.yui.adapter.database.repository.AuditEntryRepository;
import me.whereareiam.yui.api.persistence.AuditPersistence;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Keeps audit entries in {@code yui_audit_entries}; a text longer than the column is cut.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuditPersistenceAdapter implements AuditPersistence {
	private static final int CONTENT = 4000;

	private final AuditEntryRepository entries;

	@Override
	public void save(@NotNull Record record) {
		String content = record.content().length() > CONTENT ? record.content().substring(0, CONTENT) : record.content();
		entries.save(AuditEntryEntity.builder()
				.category(record.category())
				.entry(record.entry())
				.postedAt(record.postedAt())
				.content(content)
				.build());
	}

	@Override
	@Transactional(readOnly = true)
	public @NotNull List<Record> search(@Nullable AuditCategory category, @Nullable String entry, int limit) {
		// Always concrete values: PostgreSQL cannot type a null parameter.
		Set<AuditCategory> categories = category == null ? EnumSet.allOf(AuditCategory.class) : EnumSet.of(category);
		String pattern = "%" + (entry == null ? "" : entry) + "%";

		return entries.search(categories, pattern, PageRequest.of(0, limit)).stream()
				.map(found -> new Record(found.getCategory(), found.getEntry(), found.getPostedAt(), found.getContent()))
				.toList();
	}

	@Override
	public int removeBefore(@NotNull Instant instant) {
		return entries.deleteBefore(instant);
	}
}
