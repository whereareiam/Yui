package me.whereareiam.yui.util;

import me.whereareiam.yui.model.profile.UserProfile;
import me.whereareiam.yui.registry.UserProfileCacheRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@SuppressWarnings("unused")
public class Users {
	private static UserProfileCacheRegistry userProfileCacheRegistry;
	private static JDA jda;

	@Autowired
	public void init(UserProfileCacheRegistry userProfileCacheRegistry, JDA jda) {
		Users.userProfileCacheRegistry = userProfileCacheRegistry;
		Users.jda = jda;
	}

	public static Optional<UserProfile> get(long userId) {
		return userProfileCacheRegistry.getProfile(userId);
	}

	public static String getUsername(long userId) {
		User user = jda.getUserById(userId);
		if (user == null)
			return "UNKNOWN";

		return user.getName();
	}

	public static String getMention(long id) {
		return "<@" + id + ">";
	}
}
