package me.whereareiam.yui.api.persistence;

import me.whereareiam.yui.api.type.audit.AuditCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

/**
 * Keeps the audit trail in the database, when {@code audit.yml}'s {@code store} says so, for {@code /audit} to search.
 */
public interface AuditPersistence {
	void save(@NotNull Record record);

	/**
	 * @param category only entries of this category, or any
	 * @param entry    only entries whose name contains this, or any
	 * @param limit    at most this many, the newest first
	 * @return the matching entries
	 */
	@NotNull List<Record> search(@Nullable AuditCategory category, @Nullable String entry, int limit);

	/**
	 * @return how many entries posted before the instant were removed
	 */
	int removeBefore(@NotNull Instant instant);

	/**
	 * One kept entry of the audit trail.
	 *
	 * @param category its category
	 * @param entry    its name, such as {@code member.join}
	 * @param postedAt when it was posted
	 * @param content  what it said, as plain text
	 */
	record Record(@NotNull AuditCategory category, @NotNull String entry, @NotNull Instant postedAt, @NotNull String content) {
	}
}
