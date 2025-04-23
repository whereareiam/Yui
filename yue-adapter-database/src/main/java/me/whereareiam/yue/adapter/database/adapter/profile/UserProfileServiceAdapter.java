package me.whereareiam.yue.adapter.database.adapter.profile;

import jakarta.transaction.Transactional;
import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.entity.RoleEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileLanguageEntity;
import me.whereareiam.yue.adapter.database.mapper.ProfileMapper;
import me.whereareiam.yue.adapter.database.repository.LanguageRepository;
import me.whereareiam.yue.adapter.database.repository.RoleRepository;
import me.whereareiam.yue.adapter.database.repository.UserProfileRepository;
import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.service.UserProfileService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserProfileServiceAdapter implements UserProfileService {

	private final UserProfileRepository userProfileRepository;
	private final LanguageRepository languageRepository;
	private final RoleRepository roleRepository;

	@Autowired
	public UserProfileServiceAdapter(
			UserProfileRepository userProfileRepository,
			LanguageRepository languageRepository,
			RoleRepository roleRepository
	) {
		this.userProfileRepository = userProfileRepository;
		this.languageRepository = languageRepository;
		this.roleRepository = roleRepository;
	}

	@Override
	public Optional<UserProfile> createProfile(long id) {
		if (userProfileRepository.existsById(id))
			throw new IllegalArgumentException("UserProfile with id " + id + " already exists");

		UserProfileEntity userProfileEntity = UserProfileEntity.builder()
				.id(id)
				.build();

		userProfileRepository.save(userProfileEntity);

		return Optional.of(new UserProfile(id, null, new DiscordLocale[0], null));
	}

	@Override
	public Optional<UserProfile> createProfile(UserProfile userProfile) {
		if (userProfileRepository.existsById(userProfile.getId()))
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

		// If the roles are present, load or save them all, then set the userProfile's roles field to the loaded roles
		if (userProfile.getRoles() != null && userProfile.getRoles().length > 0) {
			Set<RoleEntity> roleEntities = Arrays.stream(userProfile.getRoles())
					.mapToObj(roleId -> roleRepository.findById(roleId)
							.orElseGet(() -> roleRepository.save(RoleEntity.builder().id(roleId).build())))
					.collect(Collectors.toSet());
			userProfileEntity.setRoles(roleEntities);
		}

		// Save the bare userProfile first
		userProfileRepository.save(userProfileEntity);

		// Then handle additional languages
		if (userProfile.getAdditionalLanguages() != null) {
			for (DiscordLocale locale : userProfile.getAdditionalLanguages()) {
				addAdditionalLanguage(userProfile.getId(), locale);
			}
		}

		return Optional.of(userProfile);
	}

	@Override
	public void deleteProfile(long id) {
		userProfileRepository.deleteById(id);
	}

	@Override
	public void changePrimaryLanguage(long id, DiscordLocale locale) {
		UserProfileEntity userProfileEntity = userProfileRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + id));

		LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Primary language not found: " + locale));

		userProfileEntity.setPrimaryLanguage(primaryLanguageEntity);
		userProfileRepository.save(userProfileEntity);
	}

	@Override
	public void addAdditionalLanguage(long profileId, DiscordLocale locale) {
		UserProfileEntity userProfileEntity = userProfileRepository.findById(profileId)
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
			userProfileRepository.save(userProfileEntity);
		}
	}

	@Override
	public void removeAdditionalLanguage(long profileId, DiscordLocale locale) {
		UserProfileEntity userProfileEntity = userProfileRepository.findById(profileId)
				.orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + profileId));

		if (userProfileEntity.getAdditionalLanguages() != null) {
			userProfileEntity.getAdditionalLanguages().removeIf(pl ->
					pl.getLanguageEntity().getLocale().equals(locale));
			userProfileRepository.save(userProfileEntity);
		}
	}

	@Override
	@Transactional
	public Optional<UserProfile> getProfile(long id) {
		return userProfileRepository.findById(id).map(ProfileMapper::toProfile);
	}
}
