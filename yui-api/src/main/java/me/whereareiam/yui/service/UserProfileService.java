package me.whereareiam.yui.service;

import me.whereareiam.yui.model.profile.UserProfile;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Optional;

public interface UserProfileService {
	Optional<UserProfile> createProfile(long id);

	Optional<UserProfile> createProfile(UserProfile userProfile);

	void deleteProfile(long id);

	void changePrimaryLanguage(long id, DiscordLocale locale);

	void addAdditionalLanguage(long id, DiscordLocale locale);

	void removeAdditionalLanguage(long id, DiscordLocale locale);

	void addRole(long profileId, long roleId);

	void removeRole(long profileId, long roleId);

	Optional<UserProfile> getProfile(long id);
}
