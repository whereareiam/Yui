package me.whereareiam.yui.adapter.database.adapter.profile;

import jakarta.transaction.Transactional;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.entity.RoleEntity;
import me.whereareiam.yui.adapter.database.entity.userprofile.UserProfileEntity;
import me.whereareiam.yui.adapter.database.mapper.ProfileMapper;
import me.whereareiam.yui.adapter.database.repository.LanguageRepository;
import me.whereareiam.yui.adapter.database.repository.RoleRepository;
import me.whereareiam.yui.adapter.database.repository.UserProfileRepository;
import me.whereareiam.yui.api.event.language.AdditionalLanguageAddedEvent;
import me.whereareiam.yui.api.event.language.AdditionalLanguageRemovedEvent;
import me.whereareiam.yui.api.event.language.LanguageChangeEvent;
import me.whereareiam.yui.api.event.user.UserProfileCreatedEvent;
import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.output.service.UserProfileService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserProfileServiceAdapter implements UserProfileService {
	private final UserProfileRepository userProfileRepository;
	private final LanguageRepository languageRepository;
	private final RoleRepository roleRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public UserProfileServiceAdapter(
			UserProfileRepository userProfileRepository,
			LanguageRepository languageRepository,
			RoleRepository roleRepository,
			ApplicationEventPublisher eventPublisher
	) {
		this.userProfileRepository = userProfileRepository;
		this.languageRepository = languageRepository;
		this.roleRepository = roleRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public Optional<UserProfile> createProfile(long id) {
		if (userProfileRepository.existsById(id))
			throw new IllegalArgumentException("UserProfile with id " + id + " already exists");

		UserProfileEntity userProfileEntity = UserProfileEntity.builder()
				.id(id)
				.build();

		userProfileRepository.save(userProfileEntity);

		UserProfile userProfile = new UserProfile(id, null, new DiscordLocale[0], null);
		eventPublisher.publishEvent(new UserProfileCreatedEvent(userProfile));

		return Optional.of(userProfile);
	}

	@Override
	public Optional<UserProfile> createProfile(UserProfile userProfile) {
		if (userProfileRepository.existsById(userProfile.getId()))
			throw new IllegalArgumentException("UserProfile with id " + userProfile.getId() + " already exists");

		UserProfileEntity userProfileEntity = UserProfileEntity.builder()
				.id(userProfile.getId())
				.build();

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

		eventPublisher.publishEvent(new UserProfileCreatedEvent(userProfile));

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

		DiscordLocale current = userProfileEntity.getPrimaryLanguage() == null
				? null
				: userProfileEntity.getPrimaryLanguage().getLocale();

		LanguageChangeEvent event = new LanguageChangeEvent(id, current);
		event.setLanguage(locale);

		eventPublisher.publishEvent(event);

		if (event.isCancelled()) {
			return;
		}

		DiscordLocale newLocale = event.getLanguage();
		LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(newLocale)
				.orElseThrow(() -> new IllegalArgumentException("Primary language not found: " + newLocale));

		userProfileEntity.setPrimaryLanguage(primaryLanguageEntity);
		userProfileRepository.save(userProfileEntity);
	}

	@Override
	public void addAdditionalLanguage(long profileId, DiscordLocale locale) {
		AdditionalLanguageAddedEvent event = new AdditionalLanguageAddedEvent(profileId);
		event.setLanguage(locale);

		eventPublisher.publishEvent(event);
		if (event.isCancelled())
			return;

		locale = event.getLanguage();

		DiscordLocale finalLocale = locale;
		UserProfileEntity userProfileEntity = userProfileRepository.findById(profileId)
				.orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + profileId));

		LanguageEntity languageEntity = languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Language not found: " + finalLocale));

		if (userProfileEntity.getAdditionalLanguages() == null)
			userProfileEntity.setAdditionalLanguages(new HashSet<>());

		boolean alreadyPresent = userProfileEntity.getAdditionalLanguages()
				.stream()
				.anyMatch(lang -> Objects.equals(lang.getId(), languageEntity.getId()));

		if (!alreadyPresent) {
			userProfileEntity.getAdditionalLanguages().add(languageEntity);
			userProfileRepository.save(userProfileEntity);
		}
	}

	@Override
	public void removeAdditionalLanguage(long profileId, DiscordLocale locale) {
		AdditionalLanguageRemovedEvent event = new AdditionalLanguageRemovedEvent(profileId);
		event.setLanguage(locale);

		eventPublisher.publishEvent(event);
		if (event.isCancelled())
			return;

		locale = event.getLanguage();

		DiscordLocale finalLocale = locale;
		UserProfileEntity userProfileEntity = userProfileRepository.findById(profileId)
				.orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + profileId));

		if (userProfileEntity.getAdditionalLanguages() != null &&
				userProfileEntity.getAdditionalLanguages().removeIf(language -> language.getLocale() == finalLocale)) {
			userProfileRepository.save(userProfileEntity);
		}
	}

	@Override
	public void addRole(long profileId, long roleId) {
		UserProfileEntity userProfileEntity = userProfileRepository.findById(profileId)
				.orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + profileId));

		RoleEntity roleEntity = roleRepository.findById(roleId)
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

		if (userProfileEntity.getRoles() == null)
			userProfileEntity.setRoles(new HashSet<>());

		if (!userProfileEntity.getRoles().contains(roleEntity)) {
			userProfileEntity.getRoles().add(roleEntity);
			userProfileRepository.save(userProfileEntity);
		}
	}

	@Override
	public void removeRole(long profileId, long roleId) {
		UserProfileEntity userProfileEntity = userProfileRepository.findById(profileId)
				.orElseThrow(() -> new IllegalArgumentException("UserProfile not found with id: " + profileId));

		if (userProfileEntity.getRoles() != null &&
				userProfileEntity.getRoles().removeIf(role -> role.getId() == roleId)) {
			userProfileRepository.save(userProfileEntity);
		}
	}

	@Override
	public Optional<UserProfile> getProfile(long id) {
		return userProfileRepository.findById(id).map(ProfileMapper::toProfile);
	}
}