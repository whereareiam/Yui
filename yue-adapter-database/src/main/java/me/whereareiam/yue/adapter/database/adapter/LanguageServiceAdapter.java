package me.whereareiam.yue.adapter.database.adapter;

import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.repository.LanguageRepository;
import me.whereareiam.yue.api.output.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class LanguageServiceAdapter implements LanguageService {
	private final LanguageRepository languageRepository;

	@Autowired
	public LanguageServiceAdapter(LanguageRepository languageRepository) {
		this.languageRepository = languageRepository;
	}

	@Override
	public void addLanguage(Locale locale) {
		if (!languageExists(locale))
			languageRepository.save(
					LanguageEntity.builder()
							.locale(locale)
							.build()
			);
	}

	@Override
	public void removeLanguage(Locale locale) {
		languageRepository.deleteByLocale(locale);
	}

	@Override
	public boolean languageExists(Locale locale) {
		return languageRepository.findByLocale((locale)).isPresent();
	}

	@Override
	public List<Locale> getAvailableLanguages() {
		return languageRepository.findAll()
				.stream()
				.map(LanguageEntity::getLocale)
				.toList();
	}
}
