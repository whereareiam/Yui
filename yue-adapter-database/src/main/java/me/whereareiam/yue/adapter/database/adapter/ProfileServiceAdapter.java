package me.whereareiam.yue.adapter.database.adapter;

import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.entity.profile.ProfileEntity;
import me.whereareiam.yue.adapter.database.entity.profile.ProfileLanguageEntity;
import me.whereareiam.yue.adapter.database.repository.LanguageRepository;
import me.whereareiam.yue.adapter.database.repository.ProfileRepository;
import me.whereareiam.yue.api.model.profile.Profile;
import me.whereareiam.yue.api.output.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProfileServiceAdapter implements ProfileService {
	private final ProfileRepository profileRepository;
	private final LanguageRepository languageRepository;

	@Autowired
	public ProfileServiceAdapter(ProfileRepository profileRepository, LanguageRepository languageRepository) {
		this.profileRepository = profileRepository;
		this.languageRepository = languageRepository;
	}

	@Override
	public void createProfile(Profile profile) {
		if (profileRepository.findById(profile.getId()).isPresent())
			throw new IllegalArgumentException("Profile with id " + profile.getId() + " already exists");

		ProfileEntity profileEntity = ProfileEntity.builder()
				.id(profile.getId())
				.build();

		// Set primary language only if not null
		if (profile.getPrimaryLanguage() != null) {
			LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(profile.getPrimaryLanguage())
					.orElseThrow(() -> new IllegalArgumentException("Primary language not found: " + profile.getPrimaryLanguage()));
			profileEntity.setPrimaryLanguage(primaryLanguageEntity);
		}

		profileRepository.save(profileEntity);

		// Add additional languages if provided
		if (profile.getAdditionalLanguages() != null) {
			for (Locale lang : profile.getAdditionalLanguages()) {
				addAdditionalLanguage(profile.getId(), lang);
			}
		}
	}

	@Override
	public void createProfile(long id, Locale locale, Locale[] additionalLanguages) {
		if (profileRepository.findById(id).isPresent()) {
			throw new IllegalArgumentException("Profile with id " + id + " already exists");
		}

		ProfileEntity profileEntity = ProfileEntity.builder()
				.id(id)
				.build();

		// Set primary language only if not null
		if (locale != null) {
			LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(locale)
					.orElseThrow(() -> new IllegalArgumentException("Primary language not found: " + locale));
			profileEntity.setPrimaryLanguage(primaryLanguageEntity);
		}

		profileRepository.save(profileEntity);

		// Add additional languages if provided
		if (additionalLanguages != null) {
			for (Locale lang : additionalLanguages) {
				addAdditionalLanguage(id, lang);
			}
		}
	}

	@Override
	public void changePrimaryLanguage(long id, Locale locale) {
		ProfileEntity profileEntity = profileRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Profile not found with id: " + id));

		LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Primary language not found: " + locale));

		profileEntity.setPrimaryLanguage(primaryLanguageEntity);
		profileRepository.save(profileEntity);
	}

	@Override
	public void addAdditionalLanguage(long profileId, Locale locale) {
		ProfileEntity profileEntity = profileRepository.findById(profileId)
				.orElseThrow(() -> new IllegalArgumentException("Profile not found with id: " + profileId));

		LanguageEntity languageEntity = languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Language not found: " + locale));

		// Check if this language is already added
		boolean alreadyExists = profileEntity.getAdditionalLanguages() != null &&
				profileEntity.getAdditionalLanguages().stream()
						.anyMatch(pl -> pl.getLanguageEntity().getLocale().equals(locale));

		if (!alreadyExists) {
			ProfileLanguageEntity languageLink = ProfileLanguageEntity.builder()
					.profileEntity(profileEntity)
					.languageEntity(languageEntity)
					.build();

			if (profileEntity.getAdditionalLanguages() == null) {
				profileEntity.setAdditionalLanguages(new HashSet<>());
			}

			profileEntity.getAdditionalLanguages().add(languageLink);
			profileRepository.save(profileEntity);
		}
	}

	@Override
	public void removeAdditionalLanguage(long profileId, Locale locale) {
		ProfileEntity profileEntity = profileRepository.findById(profileId)
				.orElseThrow(() -> new IllegalArgumentException("Profile not found with id: " + profileId));

		if (profileEntity.getAdditionalLanguages() != null) {
			profileEntity.getAdditionalLanguages().removeIf(pl ->
					pl.getLanguageEntity().getLocale().equals(locale));
			profileRepository.save(profileEntity);
		}
	}

	@Override
	public void deleteProfile(long id) {
		profileRepository.deleteById(id);
	}

	@Override
	public Optional<Profile> getProfile(long id) {
		return profileRepository.findById(id)
				.map(this::mapToProfile);
	}

	private Profile mapToProfile(ProfileEntity entity) {
		return new Profile(entity.getId(), entity.getPrimaryLanguage().getLocale(), entity.getAdditionalLanguages().stream()
				.map(ProfileLanguageEntity::getLanguageEntity)
				.map(LanguageEntity::getLocale)
				.toArray(Locale[]::new));
	}
}