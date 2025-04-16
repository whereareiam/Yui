package me.whereareiam.yue.api.output.service;

import me.whereareiam.yue.api.model.profile.UserProfile;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Optional;

public interface UserProfileService {
	Optional<UserProfile> createProfile(long id);

	Optional<UserProfile> createProfile(UserProfile userProfile);

	void createProfile(long id, DiscordLocale locale, DiscordLocale[] additionalLanguages);

	void deleteProfile(long id);

	void changePrimaryLanguage(long id, DiscordLocale locale);

	void addAdditionalLanguage(long id, DiscordLocale locale);

	void removeAdditionalLanguage(long id, DiscordLocale locale);

	Optional<UserProfile> getProfile(long id);
}
