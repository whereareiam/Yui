package me.whereareiam.yui.api.util;

import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.output.provider.UserProfileCacheProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@SuppressWarnings("unused")
public class Users {
	private static UserProfileCacheProvider userProfileCacheProvider;
	private static JDA jda;

	@Autowired
	public void init(UserProfileCacheProvider userProfileCacheProvider, JDA jda) {
		Users.userProfileCacheProvider = userProfileCacheProvider;
		Users.jda = jda;
	}

	public static Optional<UserProfile> get(long userId) {
		return userProfileCacheProvider.getProfile(userId);
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
