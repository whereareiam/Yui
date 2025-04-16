package me.whereareiam.yue.adapter.database.adapter.profile;

import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileLanguageEntity;
import me.whereareiam.yue.adapter.database.mapper.ProfileMapper;
import me.whereareiam.yue.adapter.database.repository.LanguageRepository;
import me.whereareiam.yue.adapter.database.repository.ProfileRepository;
import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.service.UserProfileService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class UserProfileServiceAdapter implements UserProfileService {

	private final ProfileRepository profileRepository;
	private final LanguageRepository languageRepository;

	@Autowired
	public UserProfileServiceAdapter(
			ProfileRepository profileRepository,
			LanguageRepository languageRepository
	) {
		this.profileRepository = profileRepository;
		this.languageRepository = languageRepository;
	}

	@Override
	public Optional<UserProfile> createProfile(long id) {
		if (profileRepository.existsById(id))
			throw new IllegalArgumentException("UserProfile with id " + id + " already exists");

		UserProfileEntity userProfileEntity = UserProfileEntity.builder()
				.id(id)
				.build();

		profileRepository.save(userProfileEntity);

		return Optional.of(new UserProfile(id, null, new DiscordLocale[0]));
	}

	@Override
	public Optional<UserProfile> createProfile(UserProfile userProfile) {
		if (profileRepository.existsById(userProfile.getId()))
			throw new IllegalArgumentException("UserProfile with id " + userProfile.getId() + " already exists");

		UserProfileEntity userProfileEntity = UserProfileEntity.builder()
				.id(userProfile.getId())
				.build();

		// If the primary language is present, load or throw
		if (userProfile.getPrimaryLanguage() != null) {
			LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(userProfile.getPrimaryLanguage())
					.orElseThrow(() -> new IllegalArgumentException(
							"Primary language not found: " + userProfile.getPrimaryLanguage()
					));
			userProfileEntity.setPrimaryLanguage(primaryLanguageEntity);
		}

		// Save the bare userProfile first
		profileRepository.save(userProfileEntity);

		// Then handle additional languages
		if (userProfile.getAdditionalLanguages() != null) {
			for (DiscordLocale locale : userProfile.getAdditionalLanguages()) {
				addAdditionalLanguage(userProfile.getId(), locale);
			}
		}

		return Optional.of(userProfile);
	}

	@Override
	public void createProfile(long id, DiscordLocale locale, DiscordLocale[] additionalLanguages) {
		if (profileRepository.existsById(id))
			throw new IllegalArgumentException("UserProfile with id " + id + " already exists");

		UserProfileEntity userProfileEntity = UserProfileEntity.builder()
				.id(id)
				.build();

		if (locale != null) {
			LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(locale)
					.orElseThrow(() -> new IllegalArgumentException("Primary language not found: " + locale));
			userProfileEntity.setPrimaryLanguage(primaryLanguageEntity);
		}

		profileRepository.save(userProfileEntity);

		// Then handle additional languages
		if (additionalLanguages != null) {
			for (DiscordLocale lang : additionalLanguages) {
				addAdditionalLanguage(id, lang);
			}
		}
	}

	@Override
	public void deleteProfile(long id) {
		profileRepository.deleteById(id);
	}

	@Override
	public void changePrimaryLanguage(long id, DiscordLocale locale) {
		UserProfileEntity userProfileEntity = profileRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + id));

		LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Primary language not found: " + locale));

		userProfileEntity.setPrimaryLanguage(primaryLanguageEntity);
		profileRepository.save(userProfileEntity);
	}

	@Override
	public void addAdditionalLanguage(long profileId, DiscordLocale locale) {
		UserProfileEntity userProfileEntity = profileRepository.findById(profileId)
				.orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + profileId));

		LanguageEntity languageEntity = languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Language not found: " + locale));

		boolean alreadyExists = userProfileEntity.getAdditionalLanguages() != null &&
				userProfileEntity.getAdditionalLanguages().stream()
						.anyMatch(pl -> pl.getLanguageEntity().getLocale().equals(locale));

		if (!alreadyExists) {
			UserProfileLanguageEntity languageLink = UserProfileLanguageEntity.builder()
					.userProfileEntity(userProfileEntity)
					.languageEntity(languageEntity)
					.build();

			if (userProfileEntity.getAdditionalLanguages() == null) {
				userProfileEntity.setAdditionalLanguages(new HashSet<>());
			}

			userProfileEntity.getAdditionalLanguages().add(languageLink);
			profileRepository.save(userProfileEntity);
		}
	}

	@Override
	public void removeAdditionalLanguage(long profileId, DiscordLocale locale) {
		UserProfileEntity userProfileEntity = profileRepository.findById(profileId)
				.orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + profileId));

		if (userProfileEntity.getAdditionalLanguages() != null) {
			userProfileEntity.getAdditionalLanguages().removeIf(pl ->
					pl.getLanguageEntity().getLocale().equals(locale));
			profileRepository.save(userProfileEntity);
		}
	}

	@Override
	public Optional<UserProfile> getProfile(long id) {
		return profileRepository.findById(id).map(ProfileMapper::toProfile);
	}
}
