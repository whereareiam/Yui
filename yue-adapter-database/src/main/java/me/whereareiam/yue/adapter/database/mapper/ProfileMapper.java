package me.whereareiam.yue.adapter.database.mapper;

import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.entity.RoleEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileEntity;
import me.whereareiam.yue.api.model.profile.UserProfile;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {
	public static UserProfileEntity toEntity(UserProfile userProfile, LanguageEntity primaryLang) {
		return UserProfileEntity.builder()
				.id(userProfile.getId())
				.primaryLanguage(primaryLang)
				.build();
	}

	public static UserProfile toProfile(UserProfileEntity entity) {
		DiscordLocale primaryLocale = entity.getPrimaryLanguage() != null
				? entity.getPrimaryLanguage().getLocale()
				: null;

		DiscordLocale[] additionalLocales = entity.getAdditionalLanguages() == null
				? new DiscordLocale[0]
				: entity.getAdditionalLanguages().stream()
				.map(LanguageEntity::getLocale)
				.toArray(DiscordLocale[]::new);

		long[] roleIds = entity.getRoles() == null || entity.getRoles().isEmpty()
				? null
				: entity.getRoles().stream()
				.mapToLong(RoleEntity::getId)
				.toArray();

		return new UserProfile(entity.getId(), primaryLocale, additionalLocales, roleIds);
	}

}
