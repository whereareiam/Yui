package me.whereareiam.yui.adapter.database.adapter.profile;

import me.whereareiam.yui.model.profile.UserProfile;
import me.whereareiam.yui.registry.UserProfileCacheRegistry;
import me.whereareiam.yui.service.UserProfileService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Primary
@Service
public class CachedUserProfileServiceAdapter implements UserProfileService {
	private final UserProfileService delegate;
	private final UserProfileCacheRegistry cache;

	@Autowired
	public CachedUserProfileServiceAdapter(
			@Qualifier("userProfileServiceAdapter") UserProfileService delegate,
			UserProfileCacheRegistry cache
	) {
		this.delegate = delegate;
		this.cache = cache;
	}

	@Override
	public Optional<UserProfile> createProfile(long id) {
		Optional<UserProfile> createdProfile = delegate.createProfile(id);
		createdProfile.ifPresent(profile -> cache.putProfile(id, profile));
		return createdProfile;
	}

	@Override
	public Optional<UserProfile> createProfile(UserProfile userProfile) {
		Optional<UserProfile> createdProfile = delegate.createProfile(userProfile);
		createdProfile.ifPresent(profile -> cache.putProfile(userProfile.getId(), profile));
		return createdProfile;
	}

	@Override
	public void deleteProfile(long id) {
		delegate.deleteProfile(id);
		cache.evictProfile(id);
	}

	@Override
	public void changePrimaryLanguage(long id, DiscordLocale locale) {
		delegate.changePrimaryLanguage(id, locale);
		// Refresh the updated userprofile from the delegate
		delegate.getProfile(id).ifPresent(updated -> cache.putProfile(id, updated));
	}

	@Override
	public void addAdditionalLanguage(long profileId, DiscordLocale locale) {
		delegate.addAdditionalLanguage(profileId, locale);
		delegate.getProfile(profileId).ifPresent(updated -> cache.putProfile(profileId, updated));
	}

	@Override
	public void removeAdditionalLanguage(long profileId, DiscordLocale locale) {
		delegate.removeAdditionalLanguage(profileId, locale);
		delegate.getProfile(profileId).ifPresent(updated -> cache.putProfile(profileId, updated));
	}

	@Override
	public void addRole(long profileId, long roleId) {
		delegate.addRole(profileId, roleId);
		delegate.getProfile(profileId).ifPresent(updated -> cache.putProfile(profileId, updated));
	}

	@Override
	public void removeRole(long profileId, long roleId) {
		delegate.removeRole(profileId, roleId);
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