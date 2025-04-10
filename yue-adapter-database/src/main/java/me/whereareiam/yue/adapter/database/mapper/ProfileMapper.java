package me.whereareiam.yue.adapter.database.mapper;

import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileEntity;
import me.whereareiam.yue.api.model.profile.UserProfile;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ProfileMapper {
	public UserProfileEntity toEntity(UserProfile userProfile, LanguageEntity primaryLang) {
		return UserProfileEntity.builder()
				.id(userProfile.getId())
				.primaryLanguage(primaryLang)
				.build();
	}

	public UserProfile toProfile(UserProfileEntity entity) {
		Locale primaryLocale = entity.getPrimaryLanguage() != null
				? entity.getPrimaryLanguage().getLocale()
				: null;

		Locale[] additionalLocales = entity.getAdditionalLanguages() == null
				? new Locale[0]
				: entity.getAdditionalLanguages().stream()
				.map(pl -> pl.getLanguageEntity().getLocale())
				.toArray(Locale[]::new);

		return new UserProfile(entity.getId(), primaryLocale, additionalLocales);
	}
}
