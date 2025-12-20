package me.whereareiam.yui.adapter.database.adapter;

import jakarta.transaction.Transactional;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.repository.LanguageRepository;
import me.whereareiam.yui.persistence.LanguagePersistence;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguagePersistenceAdapter implements LanguagePersistence {
	private final LanguageRepository languageRepository;

	@Autowired
	public LanguagePersistenceAdapter(LanguageRepository languageRepository) {
		this.languageRepository = languageRepository;
	}

	@Override
	public void addLanguage(DiscordLocale locale) {
		if (!languageExists(locale))
			languageRepository.save(
					LanguageEntity.builder()
							.locale(locale)
							.build()
			);
	}

	@Override
	@Transactional
	public void removeLanguage(DiscordLocale locale) {
		languageRepository.deleteByLocale(locale);
	}

	@Override
	public boolean languageExists(DiscordLocale locale) {
		return languageRepository.findByLocale(locale).isPresent();
	}

	@Override
	public List<DiscordLocale> getAvailableLanguages() {
		return languageRepository.findAll()
				.stream()
				.map(LanguageEntity::getLocale)
				.toList();
	}
}
