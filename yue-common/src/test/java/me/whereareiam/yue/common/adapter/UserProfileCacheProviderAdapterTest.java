package me.whereareiam.yue.common.adapter;

import me.whereareiam.yue.api.model.profile.UserProfile;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileCacheProviderAdapterTest {
	@Mock
	private JDA jda;

	@Mock
	private User jdaUser;

	private UserProfileCacheProviderAdapter cacheProvider;

	@BeforeEach
	void setUp() {
		cacheProvider = new UserProfileCacheProviderAdapter(jda);
	}

	@Test
	void putAndGetProfile_shouldStoreAndRetrieveProfile() {
		// Arrange
		long userId = 123L;
		UserProfile profile = new UserProfile(userId, DiscordLocale.ENGLISH_US, new DiscordLocale[0]);

		// Act
		cacheProvider.putProfile(userId, profile);
		Optional<UserProfile> result = cacheProvider.getProfile(userId);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(userId, result.get().getId());
		assertEquals(DiscordLocale.ENGLISH_US, result.get().getPrimaryLanguage());
	}

	@Test
	void getProfile_whenNotCached_shouldReturnEmpty() {
		// Act
		Optional<UserProfile> result = cacheProvider.getProfile(999L);

		// Assert
		assertTrue(result.isEmpty());
	}

	@Test
	void evictProfile_shouldRemoveFromCache() {
		// Arrange
		long userId = 123L;
		UserProfile profile = new UserProfile(userId, DiscordLocale.ENGLISH_US, new DiscordLocale[0]);
		cacheProvider.putProfile(userId, profile);

		// Act
		cacheProvider.evictProfile(userId);
		Optional<UserProfile> result = cacheProvider.getProfile(userId);

		// Assert
		assertTrue(result.isEmpty());
	}

	@Test
	void getJdaUser_whenUserExists_shouldReturnUser() {
		// Arrange
		long userId = 123L;
		when(jda.getUserById(userId)).thenReturn(jdaUser);

		// Act
		Optional<User> result = cacheProvider.getJdaUser(userId);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(jdaUser, result.get());
	}

	@Test
	void getJdaUser_whenUserDoesNotExist_shouldReturnEmpty() {
		// Arrange
		long userId = 123L;
		when(jda.getUserById(userId)).thenReturn(null);

		// Act
		Optional<User> result = cacheProvider.getJdaUser(userId);

		// Assert
		assertTrue(result.isEmpty());
	}
}