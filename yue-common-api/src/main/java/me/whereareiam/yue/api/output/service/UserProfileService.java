package me.whereareiam.yue.api.output.service;

import me.whereareiam.yue.api.model.profile.UserProfile;

import java.util.Locale;
import java.util.Optional;

public interface UserProfileService {
	Optional<UserProfile> createProfile(long id);

	Optional<UserProfile> createProfile(UserProfile userProfile);

	void createProfile(long id, Locale locale, Locale[] additionalLanguages);

	void deleteProfile(long id);

	void changePrimaryLanguage(long id, Locale locale);

	void addAdditionalLanguage(long id, Locale locale);

	void removeAdditionalLanguage(long id, Locale locale);

	Optional<UserProfile> getProfile(long id);
}
