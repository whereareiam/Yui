package me.whereareiam.yui.api.model.config.audit;

import me.whereareiam.yui.api.type.audit.AuditCategory;
import org.jetbrains.annotations.NotNull;

/**
 * Where {@code audit.yml} sends one entry instead of its own category: nowhere ({@code off}) or another category
 * ({@code system}).
 */
public sealed interface AuditRoute {
	/**
	 * The entry is not posted.
	 */
	record Off() implements AuditRoute {
	}

	/**
	 * The entry is posted with another category.
	 *
	 * @param category that category
	 */
	record To(@NotNull AuditCategory category) implements AuditRoute {
	}
}
