package me.whereareiam.yue.adapter.database.adapter.profile;

import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import me.whereareiam.yue.api.output.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class CachedUserProfileServiceAdapter implements UserProfileService {
	private final UserProfileService delegate;
	private final UserProfileCacheProvider cache;

	@Autowired
	public CachedUserProfileServiceAdapter(
			@Qualifier("userProfileServiceAdapter") UserProfileService delegate,
			UserProfileCacheProvider cache
	) {
		this.delegate = delegate;
		this.cache = cache;
	}

	@Override
	public void createProfile(UserProfile userProfile) {
		delegate.createProfile(userProfile);
		cache.putProfile(userProfile.getId(), userProfile);
	}

	@Override
	public void createProfile(long id, Locale locale, Locale[] additionalLanguages) {
		delegate.createProfile(id, locale, additionalLanguages);
		UserProfile newUserProfile = new UserProfile(id, locale, additionalLanguages);
		cache.putProfile(id, newUserProfile);
	}

	@Override
	public void deleteProfile(long id) {
		delegate.deleteProfile(id);
		cache.evictProfile(id);
	}

	@Override
	public void changePrimaryLanguage(long id, Locale locale) {
		delegate.changePrimaryLanguage(id, locale);
		// Refresh the updated userprofile from the delegate
		delegate.getProfile(id).ifPresent(updated -> cache.putProfile(id, updated));
	}

	@Override
	public void addAdditionalLanguage(long profileId, Locale locale) {
		delegate.addAdditionalLanguage(profileId, locale);
		delegate.getProfile(profileId).ifPresent(updated -> cache.putProfile(profileId, updated));
	}

	@Override
	public void removeAdditionalLanguage(long profileId, Locale locale) {
		delegate.removeAdditionalLanguage(profileId, locale);
		delegate.getProfile(profileId).ifPresent(updated -> cache.putProfile(profileId, updated));
	}

	@Override
	public Optional<UserProfile> getProfile(long id) {
		// First check cache
		Optional<UserProfile> cachedProfile = cache.getProfile(id);
		if (cachedProfile.isPresent()) {
			return cachedProfile;
		}

		// Otherwise, hit the delegate
		Optional<UserProfile> dbProfile = delegate.getProfile(id);
		dbProfile.ifPresent(profile -> cache.putProfile(id, profile));
		return dbProfile;
	}
}
