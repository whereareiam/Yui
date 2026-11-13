package me.whereareiam.yui.common.fluctlight;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.event.Cancellable;
import me.whereareiam.yui.api.event.fluctlight.FluctlightCreatedEvent;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.persistence.FluctlightPersistence.Facts;
import me.whereareiam.yui.api.persistence.FluctlightPersistence;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Loads and changes users, each in one transaction: load the stored facts, change them, save them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class FluctlightChanges {
	private final FluctlightPersistence persistence;
	private final FluctlightCache cache;
	private final ApplicationEventPublisher events;
	private final ObjectProvider<Settings> settings;

	/**
	 * Loads a user, storing them with the defaults when Yui has never seen them.
	 */
	@Transactional
	public Fluctlight load(User user) {
		Optional<Facts> stored = persistence.load(user.getIdLong());
		if (stored.isPresent()) return snapshot(user, stored.get());

		Fluctlight created = fresh(user);
		persistence.save(created.getId(), facts(created));
		events.publishEvent(new FluctlightCreatedEvent(created));
		log.debug("Stored new user {} with language {}", created.getId(), created.getPrimaryLanguage());

		return created;
	}

	/**
	 * Applies a change to the user's stored facts. The before event is published first and may veto; the after event
	 * reaches transactional listeners once the change is committed.
	 *
	 * @param given  the user the caller holds; only their identity is used, the facts are loaded again
	 * @param change what to change
	 * @param before the cancellable event, made from the user before the change
	 * @param after  the event made from the user before and after the change
	 * @return the user after the change, or the current one when vetoed or unchanged
	 */
	@Transactional
	public <B extends Cancellable, A> Fluctlight change(
			Fluctlight given,
			UnaryOperator<Fluctlight> change,
			Function<Fluctlight, B> before,
			BiFunction<Fluctlight, Fluctlight, A> after
	) {
		Fluctlight current = current(given.getUser());
		Fluctlight changed = change.apply(current);
		if (sameFacts(current, changed)) return current;

		B vetoable = before.apply(current);
		events.publishEvent(vetoable);
		if (vetoable.isCancelled()) {
			log.debug("A listener vetoed a change of user {}", current.getId());
			return current;
		}

		persistence.save(changed.getId(), facts(changed));
		events.publishEvent(after.apply(current, changed));
		cache.invalidateAfterCommit(changed.getId());

		return changed;
	}

	/**
	 * Returns a user as Yui stores one it has never seen: the default language and nothing else.
	 */
	Fluctlight fresh(User user) {
		return Fluctlight.builder()
				.user(user)
				.primaryLanguage(settings.getObject().getLocale())
				.build();
	}

	private Fluctlight current(User user) {
		return persistence.load(user.getIdLong())
				.map(facts -> snapshot(user, facts))
				.orElseGet(() -> fresh(user));
	}

	// A primary language stays unset only when its language was removed from the database; the default takes its place.
	private Fluctlight snapshot(User user, Facts facts) {
		DiscordLocale primary = facts.primaryLanguage() != null ? facts.primaryLanguage() : settings.getObject().getLocale();

		return Fluctlight.builder()
				.user(user)
				.primaryLanguage(primary)
				.additionalLanguages(facts.additionalLanguages())
				.build();
	}

	private static Facts facts(Fluctlight fluctlight) {
		return new Facts(fluctlight.getPrimaryLanguage(), fluctlight.getAdditionalLanguages());
	}

	private static boolean sameFacts(Fluctlight first, Fluctlight second) {
		return first.getPrimaryLanguage() == second.getPrimaryLanguage()
				&& first.getAdditionalLanguages().equals(second.getAdditionalLanguages());
	}
}
