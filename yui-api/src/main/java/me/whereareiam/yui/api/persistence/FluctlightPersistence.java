package me.whereareiam.yui.api.persistence;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

/**
 * Stores the facts Yui keeps about a user. It runs in the caller's transaction and neither caches nor publishes
 * events; that is {@link me.whereareiam.yui.api.fluctlight.FluctlightService}'s job.
 */
public interface FluctlightPersistence {
	/**
	 * Loads the facts about a user.
	 *
	 * @param userId the Discord id of the user
	 * @return the facts, or empty when none are stored
	 */
	@NotNull Optional<Facts> load(long userId);

	/**
	 * Stores the facts about a user, replacing those stored before.
	 *
	 * @param userId the Discord id of the user
	 * @param facts  the facts
	 * @throws IllegalArgumentException when a language is not among the languages Yui offers
	 */
	void save(long userId, @NotNull Facts facts);

	/**
	 * The facts stored about a user.
	 *
	 * @param primaryLanguage     the primary language, or {@code null} when none is stored
	 * @param additionalLanguages the further languages the user speaks
	 */
	record Facts(@Nullable DiscordLocale primaryLanguage, @NotNull Set<DiscordLocale> additionalLanguages) {
		public Facts {
			additionalLanguages = Set.copyOf(additionalLanguages);
		}
	}
}
