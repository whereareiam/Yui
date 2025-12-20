package me.whereareiam.yui.adapter.database.mapper;

import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.entity.RoleEntity;
import me.whereareiam.yui.adapter.database.entity.FluctlightEntity;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Fluctlight model and FluctlightEntity.
 * <p>
 * Note: Fluctlight requires a JDA User instance, so this mapper only handles
 * the custom data conversion. The JDA User must be provided separately.
 */
@Component
public class FluctlightMapper {
	/**
	 * Converts a Fluctlight to a FluctlightEntity for database persistence.
	 * Only custom data is converted; JDA User data is not persisted.
	 *
	 * @param fluctlight The Fluctlight to convert
	 * @return A new FluctlightEntity with custom data from the Fluctlight
	 */
	public FluctlightEntity toEntity(Fluctlight fluctlight) {
		FluctlightEntity.FluctlightEntityBuilder builder = FluctlightEntity.builder()
				.id(fluctlight.getId());

		// Note: primaryLanguage and additionalLanguages will be set by the service
		// using LanguageRepository, as we need to convert DiscordLocale to LanguageEntity
		// roles will also be set by the service using RoleRepository

		return builder.build();
	}

	/**
	 * Extracts the primary language from a FluctlightEntity.
	 *
	 * @param entity The entity to extract from
	 * @return The primary language locale, or null if not set
	 */
	public DiscordLocale extractPrimaryLanguage(FluctlightEntity entity) {
		return entity.getPrimaryLanguage() != null
				? entity.getPrimaryLanguage().getLocale()
				: null;
	}

	/**
	 * Extracts additional languages from a FluctlightEntity.
	 *
	 * @param entity The entity to extract from
	 * @return Array of additional language locales, or empty array if none
	 */
	public DiscordLocale[] extractAdditionalLanguages(FluctlightEntity entity) {
		return entity.getAdditionalLanguages() == null
				? new DiscordLocale[0]
				: entity.getAdditionalLanguages().stream()
				.map(LanguageEntity::getLocale)
				.toArray(DiscordLocale[]::new);
	}

	/**
	 * Extracts allowed role IDs from a FluctlightEntity.
	 * These are framework roles that the bot is allowed to work with,
	 * not the fluctlight's guild roles.
	 *
	 * @param entity The entity to extract from
	 * @return Array of allowed role IDs, or null if none
	 */
	public long[] extractAllowedRoles(FluctlightEntity entity) {
		return entity.getRoles() == null || entity.getRoles().isEmpty()
				? null
				: entity.getRoles().stream()
				.mapToLong(RoleEntity::getId)
				.toArray();
	}
}

