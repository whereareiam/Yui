package me.whereareiam.yue.adapter.database;

import me.whereareiam.yue.adapter.database.adapter.ProfileServiceAdapter;
import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.entity.profile.ProfileEntity;
import me.whereareiam.yue.adapter.database.entity.profile.ProfileLanguageEntity;
import me.whereareiam.yue.adapter.database.repository.LanguageRepository;
import me.whereareiam.yue.adapter.database.repository.ProfileRepository;
import me.whereareiam.yue.api.model.profile.Profile;
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
public class ProfileServiceAdapterTest {

	@Mock
	private ProfileRepository profileRepository;

	@Mock
	private LanguageRepository languageRepository;

	private ProfileServiceAdapter profileService;

	@BeforeEach
	void setUp() {
		profileService = new ProfileServiceAdapter(profileRepository, languageRepository);
	}

	@Test
	void createProfile_withProfile_whenProfileDoesNotExist_shouldCreateProfile() {
		// Arrange
		long profileId = 1L;
		Locale primaryLocale = Locale.ENGLISH;
		Locale[] additionalLocales = new Locale[]{Locale.FRENCH};
		Profile profile = new Profile(profileId, primaryLocale, additionalLocales);

		LanguageEntity primaryLanguage = LanguageEntity.builder().locale(primaryLocale).build();
		LanguageEntity frenchLanguage = LanguageEntity.builder().locale(Locale.FRENCH).build();

		ProfileEntity savedEntity = ProfileEntity.builder()
				.id(profileId)
				.primaryLanguage(primaryLanguage)
				.additionalLanguages(new HashSet<>())
				.build();

		when(profileRepository.findById(profileId))
				.thenReturn(Optional.empty())
				.thenReturn(Optional.of(savedEntity));

		when(languageRepository.findByLocale(primaryLocale)).thenReturn(Optional.of(primaryLanguage));
		when(languageRepository.findByLocale(Locale.FRENCH)).thenReturn(Optional.of(frenchLanguage));

		// Act
		profileService.createProfile(profile);

		// Assert
		// Verify save was called twice (once for profile creation, once for adding language)
		verify(profileRepository, times(2)).save(any(ProfileEntity.class));
	}

	@Test
	void createProfile_withProfile_whenProfileExists_shouldThrowException() {
		// Arrange
		long profileId = 1L;
		Profile profile = new Profile(profileId, Locale.ENGLISH, null);
		when(profileRepository.findById(profileId)).thenReturn(Optional.of(ProfileEntity.builder().build()));

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> profileService.createProfile(profile));
	}

	@Test
	void changePrimaryLanguage_whenProfileExists_shouldUpdatePrimaryLanguage() {
		// Arrange
		long profileId = 1L;
		Locale newLocale = Locale.FRENCH;

		ProfileEntity existingProfile = ProfileEntity.builder().id(profileId).build();
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

		ProfileEntity existingProfile = ProfileEntity.builder()
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
		ProfileEntity existingProfile = ProfileEntity.builder().id(profileId).build();

		Set<ProfileLanguageEntity> additionalLanguages = new HashSet<>();
		ProfileLanguageEntity languageLink = ProfileLanguageEntity.builder()
				.languageEntity(language)
				.profileEntity(existingProfile)
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

		ProfileEntity entity = ProfileEntity.builder()
				.id(profileId)
				.primaryLanguage(primaryLanguage)
				.additionalLanguages(new HashSet<>())
				.build();

		when(profileRepository.findById(profileId)).thenReturn(Optional.of(entity));

		// Act
		Optional<Profile> result = profileService.getProfile(profileId);

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
		Optional<Profile> result = profileService.getProfile(profileId);

		// Assert
		assertTrue(result.isEmpty());
	}
}