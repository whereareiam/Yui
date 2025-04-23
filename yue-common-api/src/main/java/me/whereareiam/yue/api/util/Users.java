package me.whereareiam.yue.api.util;

import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Users {
	private static UserProfileCacheProvider userProfileCacheProvider;

	@Autowired
	public void init(UserProfileCacheProvider userProfileCacheProvider) {
		Users.userProfileCacheProvider = userProfileCacheProvider;
	}

	public static Optional<UserProfile> get(long id) {
		return userProfileCacheProvider.getProfile(id);
	}
}
