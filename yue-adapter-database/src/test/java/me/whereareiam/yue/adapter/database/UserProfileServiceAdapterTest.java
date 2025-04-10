package me.whereareiam.yue.adapter.database;

import me.whereareiam.yue.adapter.database.adapter.profile.UserProfileServiceAdapter;
import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileLanguageEntity;
import me.whereareiam.yue.adapter.database.repository.LanguageRepository;
import me.whereareiam.yue.adapter.database.repository.ProfileRepository;
import me.whereareiam.yue.api.model.profile.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceAdapterTest {

	@Mock
	private ProfileRepository profileRepository;

	@Mock
	private LanguageRepository languageRepository;

	private UserProfileServiceAdapter profileService;

	@BeforeEach
	void setUp() {
		profileService = new UserProfileServiceAdapter(profileRepository, languageRepository);
	}

	@Test
	void createProfile_withProfile_whenProfileDoesNotExist_shouldCreateProfile() {
		// Arrange
		long profileId = 1L;
		Locale primaryLocale = Locale.ENGLISH;
		Locale[] additionalLocales = new Locale[]{Locale.FRENCH};
		UserProfile userProfile = new UserProfile(profileId, primaryLocale, additionalLocales);

		LanguageEntity primaryLanguage = LanguageEntity.builder().locale(primaryLocale).build();
		LanguageEntity frenchLanguage = LanguageEntity.builder().locale(Locale.FRENCH).build();

		UserProfileEntity savedEntity = UserProfileEntity.builder()
				.id(profileId)
				.primaryLanguage(primaryLanguage)
				.additionalLanguages(new HashSet<>())
				.build();

		// Mock existsById instead of relying on findById for existence check
		when(profileRepository.existsById(profileId)).thenReturn(false);

		// Always return the profile entity when findById is called
		when(profileRepository.findById(profileId)).thenReturn(Optional.of(savedEntity));

		when(languageRepository.findByLocale(primaryLocale)).thenReturn(Optional.of(primaryLanguage));
		when(languageRepository.findByLocale(Locale.FRENCH)).thenReturn(Optional.of(frenchLanguage));

		// Act
		profileService.createProfile(userProfile);

		// Assert
		verify(profileRepository, times(2)).save(any(UserProfileEntity.class));
	}

	@Test
	void createProfile_withProfile_whenProfileExists_shouldThrowException() {
		// Arrange
		long profileId = 1L;
		UserProfile userProfile = new UserProfile(profileId, Locale.ENGLISH, null);
		when(profileRepository.existsById(profileId)).thenReturn(true);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> profileService.createProfile(userProfile));
	}

	@Test
	void changePrimaryLanguage_whenProfileExists_shouldUpdatePrimaryLanguage() {
		// Arrange
		long profileId = 1L;
		Locale newLocale = Locale.FRENCH;

		UserProfileEntity existingProfile = UserProfileEntity.builder().id(profileId).build();
		LanguageEntity newLanguage = LanguageEntity.builder().locale(newLocale).build();

		when(profileRepository.findById(profileId)).thenReturn(Optional.of(existingProfile));
		when(languageRepository.findByLocale(newLocale)).thenReturn(Optional.of(newLanguage));

		// Act
		profileService.changePrimaryLanguage(profileId, newLocale);

		// Assert
		verify(profileRepository).save(existingProfile);
		assertEquals(newLanguage, existingProfile.getPrimaryLanguage());
	}

	@Test
	void addAdditionalLanguage_whenProfileExistsAndLanguageNotAdded_shouldAddLanguage() {
		// Arrange
		long profileId = 1L;
		Locale locale = Locale.FRENCH;

		UserProfileEntity existingProfile = UserProfileEntity.builder()
				.id(profileId)
				.additionalLanguages(new HashSet<>())
				.build();

		LanguageEntity language = LanguageEntity.builder().locale(locale).build();

		when(profileRepository.findById(profileId)).thenReturn(Optional.of(existingProfile));
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.of(language));

		// Act
		profileService.addAdditionalLanguage(profileId, locale);

		// Assert
		verify(profileRepository).save(existingProfile);
		assertEquals(1, existingProfile.getAdditionalLanguages().size());
	}

	@Test
	void removeAdditionalLanguage_whenProfileExists_shouldRemoveLanguage() {
		// Arrange
		long profileId = 1L;
		Locale locale = Locale.FRENCH;

		LanguageEntity language = LanguageEntity.builder().locale(locale).build();
		UserProfileEntity existingProfile = UserProfileEntity.builder().id(profileId).build();

		Set<UserProfileLanguageEntity> additionalLanguages = new HashSet<>();
		UserProfileLanguageEntity languageLink = UserProfileLanguageEntity.builder()
				.languageEntity(language)
				.userProfileEntity(existingProfile)
				.build();
		additionalLanguages.add(languageLink);

		existingProfile.setAdditionalLanguages(additionalLanguages);

		when(profileRepository.findById(profileId)).thenReturn(Optional.of(existingProfile));

		// Act
		profileService.removeAdditionalLanguage(profileId, locale);

		// Assert
		verify(profileRepository).save(existingProfile);
		assertTrue(existingProfile.getAdditionalLanguages().isEmpty());
	}

	@Test
	void deleteProfile_shouldDeleteProfileById() {
		// Arrange
		long profileId = 1L;

		// Act
		profileService.deleteProfile(profileId);

		// Assert
		verify(profileRepository).deleteById(profileId);
	}

	@Test
	void getProfile_whenProfileExists_shouldReturnProfile() {
		// Arrange
		long profileId = 1L;
		Locale primaryLocale = Locale.ENGLISH;

		LanguageEntity primaryLanguage = LanguageEntity.builder().locale(primaryLocale).build();

		UserProfileEntity entity = UserProfileEntity.builder()
				.id(profileId)
				.primaryLanguage(primaryLanguage)
				.additionalLanguages(new HashSet<>())
				.build();

		when(profileRepository.findById(profileId)).thenReturn(Optional.of(entity));

		// Act
		Optional<UserProfile> result = profileService.getProfile(profileId);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(profileId, result.get().getId());
		assertEquals(primaryLocale, result.get().getPrimaryLanguage());
	}

	@Test
	void getProfile_whenProfileDoesNotExist_shouldReturnEmpty() {
		// Arrange
		long profileId = 1L;
		when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

		// Act
		Optional<UserProfile> result = profileService.getProfile(profileId);

		// Assert
		assertTrue(result.isEmpty());
	}
}