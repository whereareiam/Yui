package me.whereareiam.yui.model.config.audit;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.type.audit.AuditCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@code audit.yml}: where each category of the audit trail is posted, and the entries that go elsewhere or
 * nowhere. An entry is one line of the trail (who did what to whom, when), such as {@code member.leave}.
 *
 * <pre>{@code
 * _version: 1
 * categories:
 *   members:
 *     channel: "123456789012345678"
 *   system:
 *     channel: "1452800701833936906"
 * entries:
 *   member.leave: off
 *   verification.failed: system
 * }</pre>
 */
@Getter
@Setter
public class AuditTrail {
	private Map<AuditCategory, Category> categories = new EnumMap<>(AuditCategory.class);
	private Map<String, AuditRoute> entries = new LinkedHashMap<>();

	/**
	 * Where an entry is posted: its own category's channel, unless {@code entries} sends it to another category or
	 * nowhere.
	 *
	 * @param category the category the entry's writer filed it under
	 * @param entry    the entry, such as {@code member.leave}
	 * @return the channel id, or empty when the entry is not posted
	 */
	public @NotNull Optional<String> channel(@NotNull AuditCategory category, @NotNull String entry) {
		AuditRoute route = entries.getOrDefault(entry, new AuditRoute.To(category));

		return switch (route) {
			case AuditRoute.Off ignored -> Optional.empty();
			case AuditRoute.To to -> Optional.ofNullable(categories.get(to.category())).map(Category::getChannel);
		};
	}

	/**
	 * A category's channel. Without one, its entries are not posted.
	 */
	@Getter
	@Setter
	public static class Category {
		private @Nullable String channel;
	}
}
