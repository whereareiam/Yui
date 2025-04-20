package me.whereareiam.yue.common.service;

import me.whereareiam.yue.api.model.profile.UserProfile;
import me.whereareiam.yue.api.output.provider.UserProfileCacheProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultUserProfileCacheProvider implements UserProfileCacheProvider {
	private final JDA jda;

	private final Map<Long, UserProfile> profiles = new ConcurrentHashMap<>();

	@Autowired
	public DefaultUserProfileCacheProvider(JDA jda) {
		this.jda = jda;
	}

	@Override
	public void putProfile(long userId, UserProfile userProfile) {
		profiles.put(userId, userProfile);
	}

	@Override
	public Optional<UserProfile> getProfile(long userId) {
		return Optional.ofNullable(profiles.get(userId));
	}

	@Override
	public void evictProfile(long userId) {
		profiles.remove(userId);
	}

	@Override
	public Optional<User> getJdaUser(long userId) {
		User user = jda.getUserById(userId);
		return Optional.ofNullable(user);
	}
}