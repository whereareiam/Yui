package me.whereareiam.yui.adapter.database.adapter;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.adapter.database.entity.FluctlightEntity;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.repository.FluctlightRepository;
import me.whereareiam.yui.adapter.database.repository.LanguageRepository;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores the facts about users in {@code yui_fluctlights} and {@code yui_fluctlights_languages}. Joins the caller's
 * transaction, or opens one of its own.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FluctlightPersistenceAdapter implements FluctlightPersistence {
	private final FluctlightRepository fluctlightRepository;
	private final LanguageRepository languageRepository;

	@Override
	public @NotNull Optional<Facts> load(long userId) {
		return fluctlightRepository.findById(userId).map(FluctlightPersistenceAdapter::facts);
	}

	@Override
	public void save(long userId, @NotNull Facts facts) {
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseGet(() -> FluctlightEntity.builder().id(userId).build());

		entity.setPrimaryLanguage(facts.primaryLanguage() == null ? null : language(facts.primaryLanguage()));
		entity.setAdditionalLanguages(facts.additionalLanguages().stream()
				.map(this::language)
				.collect(Collectors.toCollection(HashSet::new)));

		fluctlightRepository.save(entity);
	}

	private LanguageEntity language(DiscordLocale locale) {
		return languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Yui offers no language " + locale));
	}

	private static Facts facts(FluctlightEntity entity) {
		DiscordLocale primary = entity.getPrimaryLanguage() == null ? null : entity.getPrimaryLanguage().getLocale();
		Set<DiscordLocale> additional = entity.getAdditionalLanguages() == null
				? Set.of()
				: entity.getAdditionalLanguages().stream().map(LanguageEntity::getLocale).collect(Collectors.toSet());

		return new Facts(primary, additional);
	}
}
