package me.whereareiam.yui.common.role.rule;

import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import me.whereareiam.yui.common.config.provider.RolesProvider;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.model.config.languages.LanguageEntry;
import me.whereareiam.yui.model.config.languages.Languages;
import me.whereareiam.yui.model.config.roles.RoleEntry;
import me.whereareiam.yui.model.config.roles.Roles;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link LanguageRoleRule}.
 * <p>
 * Verifies that language changes properly trigger role add/remove operations.
 */
@ExtendWith(MockitoExtension.class)
class LanguageRoleRuleTest {

	@Mock
	private LanguagesProvider languagesProvider;

	@Mock
	private RolesProvider rolesProvider;

	private static FluctlightService fluctlightService;
	private static FluctlightPersistence fluctlightPersistence;

	@InjectMocks
	private LanguageRoleRule coordinator;

	private Fluctlight fluctlight;

	@BeforeAll
	static void initServices() {
		fluctlightService = mock(FluctlightService.class);
		fluctlightPersistence = mock(FluctlightPersistence.class);
		Fluctlight.initServices(fluctlightService, fluctlightPersistence);
	}

	@AfterAll
	static void cleanupServices() {
		// Reset static references
		Fluctlight.initServices(null, null);
	}

	@BeforeEach
	void setUp() {
		// Reset mock interactions before each test
		clearInvocations(fluctlightService, fluctlightPersistence);
		
		// Setup languages configuration
		Languages languages = mock(Languages.class);
		
		LanguageEntry germanEntry = new LanguageEntry();
		germanEntry.setRole(100L);
		
		LanguageEntry englishEntry = new LanguageEntry();
		englishEntry.setRole(200L);
		
		Map<DiscordLocale, LanguageEntry> localeMap = new HashMap<>();
		localeMap.put(DiscordLocale.GERMAN, germanEntry);
		localeMap.put(DiscordLocale.ENGLISH_US, englishEntry);
		
		when(languages.toLocaleMap()).thenReturn(localeMap);
		when(languagesProvider.get()).thenReturn(languages);

		// Setup roles configuration
		Roles roles = new Roles();
		
		RoleEntry germanRole = new RoleEntry();
		germanRole.setId(100L);
		germanRole.setName("German");
		
		RoleEntry englishRole = new RoleEntry();
		englishRole.setId(200L);
		englishRole.setName("English");
		
		roles.setRoles(List.of(germanRole, englishRole));
		when(rolesProvider.get()).thenReturn(roles);

		// Setup fluctlight
		User user = mock(User.class);
		when(user.getIdLong()).thenReturn(123L);
		fluctlight = new Fluctlight(user);
	}

	@Test
	void shouldAddRoleForNewLanguage() {
		FluctlightLanguageChangedEvent event = new FluctlightLanguageChangedEvent(
				fluctlight, null, DiscordLocale.GERMAN
		);

		coordinator.onLanguageChanged(event);

		verify(fluctlightService).addAllowedRole(fluctlight, 100L);
	}

	@Test
	void shouldRemoveOldRoleAndAddNewRole() {
		FluctlightLanguageChangedEvent event = new FluctlightLanguageChangedEvent(
				fluctlight, DiscordLocale.GERMAN, DiscordLocale.ENGLISH_US
		);

		coordinator.onLanguageChanged(event);

		verify(fluctlightService).removeAllowedRole(fluctlight, 100L);
		verify(fluctlightService).addAllowedRole(fluctlight, 200L);
	}

	@Test
	void shouldHandleAdditionalLanguageAdded() {
		FluctlightAdditionalLanguageAddedEvent event =
				new FluctlightAdditionalLanguageAddedEvent(fluctlight);
		event.setLanguage(DiscordLocale.GERMAN);

		coordinator.onAdditionalLanguageAdded(event);

		verify(fluctlightService).addAllowedRole(fluctlight, 100L);
	}

	@Test
	void shouldHandleAdditionalLanguageRemoved() {
		FluctlightAdditionalLanguageRemovedEvent event =
				new FluctlightAdditionalLanguageRemovedEvent(fluctlight);
		event.setLanguage(DiscordLocale.GERMAN);

		coordinator.onAdditionalLanguageRemoved(event);

		verify(fluctlightService).removeAllowedRole(fluctlight, 100L);
	}

	@Test
	void shouldHandleMultipleLanguageChangesSequentially() {
		// Simulate rapid language changes
		FluctlightLanguageChangedEvent event1 = new FluctlightLanguageChangedEvent(
				fluctlight, null, DiscordLocale.GERMAN
		);
		FluctlightLanguageChangedEvent event2 = new FluctlightLanguageChangedEvent(
				fluctlight, DiscordLocale.GERMAN, DiscordLocale.ENGLISH_US
		);
		FluctlightLanguageChangedEvent event3 = new FluctlightLanguageChangedEvent(
				fluctlight, DiscordLocale.ENGLISH_US, DiscordLocale.GERMAN
		);

		coordinator.onLanguageChanged(event1);
		coordinator.onLanguageChanged(event2);
		coordinator.onLanguageChanged(event3);

		// Verify all role changes were queued
		verify(fluctlightService, times(2)).addAllowedRole(fluctlight, 100L);
		verify(fluctlightService, times(1)).removeAllowedRole(fluctlight, 100L);
		verify(fluctlightService, times(1)).addAllowedRole(fluctlight, 200L);
		verify(fluctlightService, times(1)).removeAllowedRole(fluctlight, 200L);
	}
}
