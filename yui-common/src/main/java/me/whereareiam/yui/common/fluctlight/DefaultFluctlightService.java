package me.whereareiam.yui.common.fluctlight;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.event.fluctlight.FluctlightClearEvent;
import me.whereareiam.yui.api.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightAdditionalLanguageAddEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightAdditionalLanguageRemoveEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightLanguageChangeEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.api.fluctlight.FluctlightService;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Serves users from the cache and hands each change to {@link FluctlightChanges}, which runs it in one transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultFluctlightService implements FluctlightService {
	private final JDA jda;
	private final FluctlightCache cache;
	private final FluctlightChanges changes;

	@Override
	public @NotNull Optional<Fluctlight> get(long userId) {
		Optional<Fluctlight> cached = cache.find(userId);
		if (cached.isPresent()) return cached;

		Optional<Fluctlight> loaded = user(userId).map(changes::load);
		loaded.ifPresent(cache::put);

		return loaded;
	}

	@Override
	public @NotNull Fluctlight changePrimaryLanguage(@NotNull Fluctlight fluctlight, @NotNull DiscordLocale language) {
		return changes.change(
				fluctlight,
				current -> current.toBuilder()
						.primaryLanguage(language)
						.additionalLanguages(without(current.getAdditionalLanguages(), language))
						.build(),
				current -> new FluctlightLanguageChangeEvent(current, language),
				(previous, changed) -> new FluctlightLanguageChangedEvent(changed, previous.getPrimaryLanguage())
		);
	}

	@Override
	public @NotNull Fluctlight addAdditionalLanguage(@NotNull Fluctlight fluctlight, @NotNull DiscordLocale language) {
		return changes.change(
				fluctlight,
				current -> current.speaks(language)
						? current
						: current.toBuilder().additionalLanguages(with(current.getAdditionalLanguages(), language)).build(),
				current -> new FluctlightAdditionalLanguageAddEvent(current, language),
				(_, changed) -> new FluctlightAdditionalLanguageAddedEvent(changed, language)
		);
	}

	@Override
	public @NotNull Fluctlight removeAdditionalLanguage(@NotNull Fluctlight fluctlight, @NotNull DiscordLocale language) {
		return changes.change(
				fluctlight,
				current -> current.toBuilder()
						.additionalLanguages(without(current.getAdditionalLanguages(), language))
						.build(),
				current -> new FluctlightAdditionalLanguageRemoveEvent(current, language),
				(_, changed) -> new FluctlightAdditionalLanguageRemovedEvent(changed, language)
		);
	}

	@Override
	public @NotNull Fluctlight clear(@NotNull Fluctlight fluctlight) {
		return changes.change(
				fluctlight,
				current -> changes.fresh(current.getUser()),
				FluctlightClearEvent::new,
				FluctlightClearedEvent::new
		);
	}

	// Discord's cache holds only users who share a server with Yui and were seen since it started.
	private Optional<User> user(long userId) {
		User cached = jda.getUserById(userId);
		if (cached != null) return Optional.of(cached);

		try {
			return Optional.of(jda.retrieveUserById(userId).complete());
		} catch (ErrorResponseException exception) {
			if (exception.getErrorResponse() != ErrorResponse.UNKNOWN_USER) throw exception;

			log.debug("Discord knows no user {}", userId);
			return Optional.empty();
		}
	}

	private static Set<DiscordLocale> with(Set<DiscordLocale> languages, DiscordLocale language) {
		Set<DiscordLocale> result = new HashSet<>(languages);
		result.add(language);

		return result;
	}

	private static Set<DiscordLocale> without(Set<DiscordLocale> languages, DiscordLocale language) {
		Set<DiscordLocale> result = new HashSet<>(languages);
		result.remove(language);

		return result;
	}
}
