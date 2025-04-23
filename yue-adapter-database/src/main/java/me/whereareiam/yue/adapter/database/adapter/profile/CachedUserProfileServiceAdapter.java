package me.whereareiam.yue.adapter.database.adapter.profile;

import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import me.whereareiam.yue.api.output.service.UserProfileService;
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
