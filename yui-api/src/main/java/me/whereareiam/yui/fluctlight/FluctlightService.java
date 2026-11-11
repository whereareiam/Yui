package me.whereareiam.yui.fluctlight;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Reads and changes what Yui knows about a user.
 * <p>
 * Each change runs in one transaction: it loads the stored facts, applies the change and saves them. Before the change
 * a cancellable event is published, whose listeners may only veto; after it, an event that listeners receive once the
 * transaction committed, through {@link org.springframework.transaction.event.TransactionalEventListener}. A change
 * returns the new snapshot, or the current one when a listener vetoed it or nothing changed.
 * <pre>{@code
 * @TransactionalEventListener
 * public void onLanguageChanged(FluctlightLanguageChangedEvent event) {
 *     // side effects belong here; a failure is logged and does not undo the change
 * }
 * }</pre>
 */
public interface FluctlightService {
	/**
	 * Returns the user, loading them from the database, and from Discord when Discord's cache does not hold them. A
	 * user Yui has never seen is stored with the default language and announced with a
	 * {@link me.whereareiam.yui.event.fluctlight.FluctlightCreatedEvent}.
	 *
	 * @param userId the Discord id of the user
	 * @return the user, or empty when Discord knows no user with that id
	 */
	@NotNull Optional<Fluctlight> get(long userId);

	/**
	 * Changes the user's primary language. A language that was additional stops being additional.
	 *
	 * @param fluctlight the user
	 * @param language   the new primary language
	 * @return the user after the change
	 */
	@NotNull Fluctlight changePrimaryLanguage(@NotNull Fluctlight fluctlight, @NotNull DiscordLocale language);

	/**
	 * Adds a language the user speaks besides their primary one. Adding the primary language changes nothing.
	 *
	 * @param fluctlight the user
	 * @param language   the language to add
	 * @return the user after the change
	 */
	@NotNull Fluctlight addAdditionalLanguage(@NotNull Fluctlight fluctlight, @NotNull DiscordLocale language);

	/**
	 * Removes a language the user speaks besides their primary one.
	 *
	 * @param fluctlight the user
	 * @param language   the language to remove
	 * @return the user after the change
	 */
	@NotNull Fluctlight removeAdditionalLanguage(@NotNull Fluctlight fluctlight, @NotNull DiscordLocale language);

	/**
	 * Resets everything Yui knows about the user to the defaults of a new user.
	 *
	 * @param fluctlight the user
	 * @return the user after the reset
	 */
	@NotNull Fluctlight clear(@NotNull Fluctlight fluctlight);
}
