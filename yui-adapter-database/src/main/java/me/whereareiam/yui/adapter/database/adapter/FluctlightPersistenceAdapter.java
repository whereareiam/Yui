package me.whereareiam.yui.adapter.database.adapter;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.entity.RoleEntity;
import me.whereareiam.yui.adapter.database.entity.FluctlightEntity;
import me.whereareiam.yui.adapter.database.mapper.FluctlightMapper;
import me.whereareiam.yui.adapter.database.repository.FluctlightRepository;
import me.whereareiam.yui.adapter.database.repository.LanguageRepository;
import me.whereareiam.yui.adapter.database.repository.RoleRepository;
import me.whereareiam.yui.event.language.AdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.language.AdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.language.LanguageChangeEvent;
import me.whereareiam.yui.model.fluctlight.FluctlightData;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Database adapter for FluctlightPersistence.
 * <p>
 * This adapter handles ONLY database operations for Fluctlight custom data.
 * It does not handle caching, JDA User retrieval, or business logic.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FluctlightPersistenceAdapter implements FluctlightPersistence {
	private final FluctlightRepository fluctlightRepository;
	private final LanguageRepository languageRepository;
	private final RoleRepository roleRepository;
	private final FluctlightMapper fluctlightMapper;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public Optional<FluctlightData> loadData(long userId) {
		return fluctlightRepository.findById(userId)
				.map(entity -> new FluctlightData(
						fluctlightMapper.extractPrimaryLanguage(entity),
						fluctlightMapper.extractAdditionalLanguages(entity),
						fluctlightMapper.extractAllowedRoles(entity)
				));
	}

	@Override
	public void saveData(long userId, FluctlightData data) {
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseGet(() -> FluctlightEntity.builder().id(userId).build());

		// Update primary language
		if (data.getPrimaryLanguage() != null) {
			LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(data.getPrimaryLanguage())
					.orElseThrow(() -> new IllegalArgumentException(
							"Primary language not found: " + data.getPrimaryLanguage()
					));
			entity.setPrimaryLanguage(primaryLanguageEntity);
		} else {
			entity.setPrimaryLanguage(null);
		}

		// Update additional languages
		if (data.getAdditionalLanguages() != null && data.getAdditionalLanguages().length > 0) {
			Set<LanguageEntity> additionalLanguageEntities = Arrays.stream(data.getAdditionalLanguages())
					.map(locale -> languageRepository.findByLocale(locale)
							.orElseThrow(() -> new IllegalArgumentException("Language not found: " + locale)))
					.collect(Collectors.toSet());
			entity.setAdditionalLanguages(additionalLanguageEntities);
		} else {
			entity.setAdditionalLanguages(new HashSet<>());
		}

		// Update allowed roles (stored as roles in entity)
		if (data.getAllowedRoles() != null && data.getAllowedRoles().length > 0) {
			Set<RoleEntity> roleEntities = Arrays.stream(data.getAllowedRoles())
					.mapToObj(roleId -> roleRepository.findById(roleId)
							.orElseGet(() -> roleRepository.save(RoleEntity.builder().id(roleId).build())))
					.collect(Collectors.toSet());
			entity.setRoles(roleEntities);
		} else {
			entity.setRoles(new HashSet<>());
		}

		fluctlightRepository.save(entity);
	}

	@Override
	public void deleteById(long userId) {
		fluctlightRepository.deleteById(userId);
	}

	@Override
	public boolean existsById(long userId) {
		return fluctlightRepository.existsById(userId);
	}

	@Override
	public void updatePrimaryLanguage(long userId, DiscordLocale locale) {
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Fluctlight not found with id: " + userId));

		DiscordLocale current = entity.getPrimaryLanguage() == null
				? null
				: entity.getPrimaryLanguage().getLocale();

		LanguageChangeEvent event = new LanguageChangeEvent(userId, current);
		event.setLanguage(locale);

		eventPublisher.publishEvent(event);

		if (event.isCancelled())
			return;

		DiscordLocale newLocale = event.getLanguage();
		LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(newLocale)
				.orElseThrow(() -> new IllegalArgumentException("Primary language not found: " + newLocale));

		entity.setPrimaryLanguage(primaryLanguageEntity);
		fluctlightRepository.save(entity);
	}

	@Override
	public void addAdditionalLanguage(long userId, DiscordLocale locale) {
		AdditionalLanguageAddedEvent event = new AdditionalLanguageAddedEvent(userId);
		event.setLanguage(locale);

		eventPublisher.publishEvent(event);
		if (event.isCancelled())
			return;

		locale = event.getLanguage();

		DiscordLocale finalLocale = locale;
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Fluctlight not found with id: " + userId));

		LanguageEntity languageEntity = languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Language not found: " + finalLocale));

		if (entity.getAdditionalLanguages() == null)
			entity.setAdditionalLanguages(new HashSet<>());

		boolean alreadyPresent = entity.getAdditionalLanguages()
				.stream()
				.anyMatch(lang -> Objects.equals(lang.getId(), languageEntity.getId()));

		if (!alreadyPresent) {
			entity.getAdditionalLanguages().add(languageEntity);
			fluctlightRepository.save(entity);
		}
	}

	@Override
	public void removeAdditionalLanguage(long userId, DiscordLocale locale) {
		AdditionalLanguageRemovedEvent event = new AdditionalLanguageRemovedEvent(userId);
		event.setLanguage(locale);

		eventPublisher.publishEvent(event);
		if (event.isCancelled())
			return;

		locale = event.getLanguage();

		DiscordLocale finalLocale = locale;
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Fluctlight not found with id: " + userId));

		if (entity.getAdditionalLanguages() != null &&
				entity.getAdditionalLanguages().removeIf(language -> language.getLocale() == finalLocale)) {
			fluctlightRepository.save(entity);
		}
	}

	@Override
	public void addAllowedRole(long userId, long roleId) {
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Fluctlight not found with id: " + userId));

		RoleEntity roleEntity = roleRepository.findById(roleId)
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

		if (entity.getRoles() == null)
			entity.setRoles(new HashSet<>());

		if (!entity.getRoles().contains(roleEntity)) {
			entity.getRoles().add(roleEntity);
			fluctlightRepository.save(entity);
		}
	}

	@Override
	public void removeAllowedRole(long userId, long roleId) {
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Fluctlight not found with id: " + userId));

		if (entity.getRoles() != null &&
				entity.getRoles().removeIf(role -> role.getId() == roleId)) {
			fluctlightRepository.save(entity);
		}
	}
}

