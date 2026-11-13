package me.whereareiam.yui.service;

import me.whereareiam.yui.type.audit.AuditCategory;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Posts entries of the audit trail where {@code audit.yml} says: the channel of the entry's category, unless the file
 * sends that entry to another category or nowhere. Most code writes entries through {@link me.whereareiam.yui.util.Audit}.
 *
 * <pre>{@code
 * auditService.audit(AuditCategory.MEMBERS, "member.join", MessageCreateData.fromContent("A member joined"));
 * }</pre>
 */
public interface AuditService {
	/**
	 * Posts an entry. An entry that is not posted completes at once.
	 *
	 * @param category the category its writer files it under
	 * @param entry    the entry, such as {@code member.join}
	 * @param message  what is posted
	 * @return completes once the message is posted, or failed to be
	 */
	@NotNull CompletableFuture<Void> audit(@NotNull AuditCategory category, @NotNull String entry, @NotNull MessageCreateData message);

	/**
	 * @param category the category its writer files it under
	 * @param entry    the entry
	 * @return the channel the entry is posted to, or empty when it is not posted
	 */
	@NotNull Optional<String> channel(@NotNull AuditCategory category, @NotNull String entry);

	/**
	 * Whether an entry is posted at all, to skip building one that is not.
	 *
	 * @param category the category its writer files it under
	 * @param entry    the entry
	 * @return true when it has a channel
	 */
	default boolean isPosted(@NotNull AuditCategory category, @NotNull String entry) {
		return channel(category, entry).isPresent();
	}
}
