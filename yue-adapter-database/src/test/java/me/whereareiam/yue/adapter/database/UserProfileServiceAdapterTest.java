package me.whereareiam.yue.adapter.database;

import me.whereareiam.yue.adapter.database.adapter.profile.UserProfileServiceAdapter;
import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.entity.RoleEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileEntity;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileLanguageEntity;
import me.whereareiam.yue.adapter.database.repository.LanguageRepository;
import me.whereareiam.yue.adapter.database.repository.RoleRepository;
import me.whereareiam.yue.adapter.database.repository.UserProfileRepository;
import me.whereareiam.yue.api.model.profile.UserProfile;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceAdapterTest {

	@Mock
	private UserProfileRepository userProfileRepository;
	@Mock
	private LanguageRepository languageRepository;
	@Mock
	private RoleRepository roleRepository;

	private UserProfileServiceAdapter profileService;

	@BeforeEach
	void setUp() {
		profileService = new UserProfileServiceAdapter(
				userProfileRepository,
				languageRepository,
				roleRepository
		);
	}

	@Test
	void createProfile_withId_whenProfileDoesNotExist_shouldCreateProfile() {
		long profileId = 1L;
		when(userProfileRepository.existsById(profileId)).thenReturn(false);

		when(userProfileRepository.save(any(UserProfileEntity.class)))
				.thenAnswer(inv -> inv.getArgument(0));

		Optional<UserProfile> result = profileService.createProfile(profileId);

		assertTrue(result.isPresent());
		UserProfile profile = result.get();
		assertEquals(profileId, profile.getId());
		assertNull(profile.getPrimaryLanguage());
		assertEquals(0, profile.getAdditionalLanguages().length);
		assertNull(profile.getRoles());
		verify(userProfileRepository).save(any(UserProfileEntity.class));
		verifyNoInteractions(roleRepository);
	}

	@Test
	void createProfile_withId_whenProfileExists_shouldThrowException() {
		long profileId = 1L;
		when(userProfileRepository.existsById(profileId)).thenReturn(true);

		assertThrows(IllegalArgumentException.class,
				() -> profileService.createProfile(profileId));

		verify(userProfileRepository, never()).save(any());
	}

	@Test
	void createProfile_withProfile_whenProfileDoesNotExist_shouldCreateProfile() {
		long profileId = 1L;
		DiscordLocale primaryLocale = DiscordLocale.ENGLISH_US;
		DiscordLocale[] additionalLocales = {DiscordLocale.FRENCH};
		long[] rolesArr = {5L, 6L};

		UserProfile userProfile = new UserProfile(
				profileId, primaryLocale, additionalLocales, rolesArr);

		// languages ---------------------------------------------------------
		LanguageEntity english = LanguageEntity.builder().locale(primaryLocale).build();
		LanguageEntity french = LanguageEntity.builder().locale(DiscordLocale.FRENCH).build();
		when(languageRepository.findByLocale(primaryLocale)).thenReturn(Optional.of(english));
		when(languageRepository.findByLocale(DiscordLocale.FRENCH)).thenReturn(Optional.of(french));

		// roles -------------------------------------------------------------
		RoleEntity role5 = RoleEntity.builder().id(5L).build();
		RoleEntity role6 = RoleEntity.builder().id(6L).build();
		when(roleRepository.findById(5L)).thenReturn(Optional.empty());
		when(roleRepository.findById(6L)).thenReturn(Optional.of(role6));  // mix of present / absent
		when(roleRepository.save(argThat(r -> r.getId() == 5L))).thenReturn(role5);

		// profile existence -------------------------------------------------
		when(userProfileRepository.existsById(profileId)).thenReturn(false);

		when(userProfileRepository.save(any(UserProfileEntity.class)))
				.thenAnswer(inv -> inv.getArgument(0));

		profileService.createProfile(userProfile);

		verify(roleRepository).save(role5);
		verify(userProfileRepository, times(2)).save(any(UserProfileEntity.class));
	}

	@Test
	void createProfile_withProfile_whenProfileExists_shouldThrowException() {
		long profileId = 1L;
		UserProfile userProfile =
				new UserProfile(profileId, DiscordLocale.ENGLISH_US, null, null);

		when(userProfileRepository.existsById(profileId)).thenReturn(true);

		assertThrows(IllegalArgumentException.class,
				() -> profileService.createProfile(userProfile));
	}

	@Test
	void changePrimaryLanguage_whenProfileExists_shouldUpdatePrimaryLanguage() {
		long profileId = 1L;
		DiscordLocale newLocale = DiscordLocale.FRENCH;

		UserProfileEntity entity = UserProfileEntity.builder().id(profileId).build();
		LanguageEntity lang = LanguageEntity.builder().locale(newLocale).build();

		when(userProfileRepository.findById(profileId)).thenReturn(Optional.of(entity));
		when(languageRepository.findByLocale(newLocale)).thenReturn(Optional.of(lang));

		profileService.changePrimaryLanguage(profileId, newLocale);

		verify(userProfileRepository).save(entity);
		assertEquals(lang, entity.getPrimaryLanguage());
	}

	@Test
	void addAdditionalLanguage_whenProfileExistsAndLanguageNotAdded_shouldAddLanguage() {
		long profileId = 1L;
		DiscordLocale locale = DiscordLocale.FRENCH;

		UserProfileEntity entity = UserProfileEntity.builder()
				.id(profileId)
				.additionalLanguages(new HashSet<>())
				.build();

		LanguageEntity lang = LanguageEntity.builder().locale(locale).build();

		when(userProfileRepository.findById(profileId)).thenReturn(Optional.of(entity));
		when(languageRepository.findByLocale(locale)).thenReturn(Optional.of(lang));

		profileService.addAdditionalLanguage(profileId, locale);

		verify(userProfileRepository).save(entity);
		assertEquals(1, entity.getAdditionalLanguages().size());
	}

	@Test
	void removeAdditionalLanguage_whenProfileExists_shouldRemoveLanguage() {
		long profileId = 1L;
		DiscordLocale locale = DiscordLocale.FRENCH;

		LanguageEntity lang = LanguageEntity.builder().locale(locale).build();
		UserProfileEntity entity = UserProfileEntity.builder().id(profileId).build();

		Set<UserProfileLanguageEntity> links = new HashSet<>();
		links.add(UserProfileLanguageEntity.builder()
				.languageEntity(lang)
				.userProfileEntity(entity)
				.build());
		entity.setAdditionalLanguages(links);

		when(userProfileRepository.findById(profileId)).thenReturn(Optional.of(entity));

		profileService.removeAdditionalLanguage(profileId, locale);

		verify(userProfileRepository).save(entity);
		assertTrue(entity.getAdditionalLanguages().isEmpty());
	}

	@Test
	void deleteProfile_shouldDeleteProfileById() {
		long profileId = 1L;
		profileService.deleteProfile(profileId);
		verify(userProfileRepository).deleteById(profileId);
	}

	@Test
	void getProfile_whenProfileExists_shouldReturnProfile() {
		long profileId = 1L;
		DiscordLocale locale = DiscordLocale.ENGLISH_US;

		LanguageEntity lang = LanguageEntity.builder().locale(locale).build();
		UserProfileEntity entity = UserProfileEntity.builder()
				.id(profileId)
				.primaryLanguage(lang)
				.build();

		when(userProfileRepository.findById(profileId)).thenReturn(Optional.of(entity));

		Optional<UserProfile> result = profileService.getProfile(profileId);

		assertTrue(result.isPresent());
		UserProfile profile = result.get();
		assertEquals(profileId, profile.getId());
		assertEquals(locale, profile.getPrimaryLanguage());
		assertNull(profile.getRoles());
	}

	@Test
	void getProfile_whenProfileDoesNotExist_shouldReturnEmpty() {
		when(userProfileRepository.findById(1L)).thenReturn(Optional.empty());
		assertTrue(profileService.getProfile(1L).isEmpty());
	}
}