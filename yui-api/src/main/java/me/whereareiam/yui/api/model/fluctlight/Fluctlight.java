package me.whereareiam.yui.api.model.fluctlight;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A snapshot of what Yui knows about a Discord user: the user and the facts kept about them, such as the languages
 * they speak.
 * <p>
 * A snapshot never changes. A change goes through {@link me.whereareiam.yui.api.fluctlight.FluctlightService}, which saves
 * it and returns a new snapshot; one taken earlier keeps the values it was taken with. Plugins keep their own facts
 * about a user in their own tables.
 * <pre>{@code
 * Fluctlight fluctlight = fluctlightService.get(userId).orElseThrow();
 * Fluctlight changed = fluctlightService.changePrimaryLanguage(fluctlight, DiscordLocale.GERMAN);
 * }</pre>
 */
@Getter
public final class Fluctlight {
	private final @NotNull User user;
	private final @NotNull DiscordLocale primaryLanguage;
	private final @NotNull Set<DiscordLocale> additionalLanguages;

	/**
	 * Creates a snapshot.
	 *
	 * @param user                the Discord user
	 * @param primaryLanguage     the language Yui speaks to the user in
	 * @param additionalLanguages further languages the user speaks; never holds the primary language
	 */
	@Builder(toBuilder = true)
	private Fluctlight(
			@NotNull User user,
			@NotNull DiscordLocale primaryLanguage,
			Set<DiscordLocale> additionalLanguages
	) {
		this.user = user;
		this.primaryLanguage = primaryLanguage;
		this.additionalLanguages = additionalLanguages == null ? Set.of() : Set.copyOf(additionalLanguages);
	}

	/**
	 * Returns the user's Discord id.
	 *
	 * @return the id
	 */
	public long getId() {
		return user.getIdLong();
	}

	/**
	 * Returns the user's Discord name.
	 *
	 * @return the name
	 */
	public @NotNull String getName() {
		return user.getName();
	}

	/**
	 * Returns the mention of the user, such as {@code <@123456789>}.
	 *
	 * @return the mention
	 */
	public @NotNull String getAsMention() {
		return user.getAsMention();
	}

	/**
	 * Tells whether the user speaks a language, as their primary or as an additional one.
	 *
	 * @param language the language
	 * @return whether they speak it
	 */
	public boolean speaks(@NotNull DiscordLocale language) {
		return primaryLanguage == language || additionalLanguages.contains(language);
	}
}
