package me.whereareiam.yui.adapter.database.adapter;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.adapter.database.entity.FluctlightEntity;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.entity.RoleEntity;
import me.whereareiam.yui.adapter.database.mapper.FluctlightMapper;
import me.whereareiam.yui.adapter.database.repository.FluctlightRepository;
import me.whereareiam.yui.adapter.database.repository.LanguageRepository;
import me.whereareiam.yui.adapter.database.repository.RoleRepository;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightData;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Service;

import java.util.*;
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

	@Override
	public Optional<FluctlightData> loadData(Fluctlight fluctlight) {
		long userId = fluctlight.getId();
		return fluctlightRepository.findById(userId)
				.map(entity -> new FluctlightData(
						fluctlightMapper.extractPrimaryLanguage(entity),
						fluctlightMapper.extractAdditionalLanguages(entity),
						fluctlightMapper.extractAllowedRoles(entity)
				));
	}

	@Override
	public void saveData(Fluctlight fluctlight, FluctlightData data) {
		long userId = fluctlight.getId();
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
	public void deleteById(Fluctlight fluctlight) {
		fluctlightRepository.deleteById(fluctlight.getId());
	}

	@Override
	public boolean existsById(Fluctlight fluctlight) {
		return fluctlightRepository.existsById(fluctlight.getId());
	}

	@Override
	public void updatePrimaryLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		long userId = fluctlight.getId();
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseGet(() -> FluctlightEntity.builder().id(userId).build());

		LanguageEntity primaryLanguageEntity = languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Primary language not found: " + locale));

		entity.setPrimaryLanguage(primaryLanguageEntity);
		fluctlightRepository.save(entity);
	}

	@Override
	public void addAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		long userId = fluctlight.getId();
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Fluctlight not found with id: " + userId));

		LanguageEntity languageEntity = languageRepository.findByLocale(locale)
				.orElseThrow(() -> new IllegalArgumentException("Language not found: " + locale));

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
	public void removeAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		long userId = fluctlight.getId();
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Fluctlight not found with id: " + userId));

		if (entity.getAdditionalLanguages() != null &&
				entity.getAdditionalLanguages().removeIf(language -> language.getLocale() == locale)) {
			fluctlightRepository.save(entity);
		}
	}

	@Override
	public void updateAdditionalLanguages(Fluctlight fluctlight, DiscordLocale[] locales) {
		long userId = fluctlight.getId();
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseGet(() -> FluctlightEntity.builder().id(userId).build());

		if (locales != null && locales.length > 0) {
			Set<LanguageEntity> additionalLanguageEntities = Arrays.stream(locales)
					.filter(Objects::nonNull)
					.map(locale -> languageRepository.findByLocale(locale)
							.orElseThrow(() -> new IllegalArgumentException("Language not found: " + locale)))
					.collect(Collectors.toSet());
			entity.setAdditionalLanguages(additionalLanguageEntities);
		} else {
			entity.setAdditionalLanguages(new HashSet<>());
		}

		fluctlightRepository.save(entity);
	}

	@Override
	public void addAllowedRole(Fluctlight fluctlight, long roleId) {
		long userId = fluctlight.getId();
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
	public void removeAllowedRole(Fluctlight fluctlight, long roleId) {
		long userId = fluctlight.getId();
		FluctlightEntity entity = fluctlightRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Fluctlight not found with id: " + userId));

		if (entity.getRoles() != null &&
				entity.getRoles().removeIf(role -> role.getId() == roleId)) {
			fluctlightRepository.save(entity);
		}
	}
}
