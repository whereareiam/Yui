package me.whereareiam.yui.common.role.rule;

import me.whereareiam.yui.api.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.api.model.config.languages.Language;
import me.whereareiam.yui.api.model.config.languages.Languages;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.role.FactRoles;
import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LanguageRoleRuleTest {
	private static final long USER = 123L;

	private FactRoles roles;
	private User user;
	private LanguageRoleRule rule;

	@BeforeEach
	void setUp() {
		roles = mock(FactRoles.class);
		when(roles.hold(anyLong(), anyString(), anyBoolean(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

		LanguagesProvider languages = mock(LanguagesProvider.class);
		when(languages.get()).thenReturn(new Languages(Map.of(
				DiscordLocale.GERMAN, language("100"),
				DiscordLocale.ENGLISH_US, language("200")
		)));

		user = mock(User.class);
		when(user.getIdLong()).thenReturn(USER);
		rule = new LanguageRoleRule(roles, languages);
	}

	@Test
	void languageChanged_TakesThePreviousRoleAndGivesTheNew() {
		rule.onLanguageChanged(new FluctlightLanguageChangedEvent(fluctlight(DiscordLocale.GERMAN, Set.of()), DiscordLocale.ENGLISH_US));

		verify(roles).hold(USER, "200", false, "language en-US");
		verify(roles).hold(USER, "100", true, "language de");
	}

	@Test
	void languageChanged_KeepsThePreviousRoleWhenItIsStillSpoken() {
		rule.onLanguageChanged(new FluctlightLanguageChangedEvent(fluctlight(DiscordLocale.GERMAN, Set.of(DiscordLocale.ENGLISH_US)), DiscordLocale.ENGLISH_US));

		verify(roles).hold(USER, "200", true, "language en-US");
	}

	@Test
	void additionalLanguageAdded_GivesItsRole() {
		rule.onAdditionalLanguageAdded(new FluctlightAdditionalLanguageAddedEvent(fluctlight(DiscordLocale.ENGLISH_US, Set.of(DiscordLocale.GERMAN)), DiscordLocale.GERMAN));

		verify(roles).hold(USER, "100", true, "language de");
	}

	@Test
	void additionalLanguageRemoved_TakesItsRole() {
		rule.onAdditionalLanguageRemoved(new FluctlightAdditionalLanguageRemovedEvent(fluctlight(DiscordLocale.ENGLISH_US, Set.of()), DiscordLocale.GERMAN));

		verify(roles).hold(USER, "100", false, "language de");
	}

	@Test
	void languageWithoutRole_ChangesNoRole() {
		rule.onAdditionalLanguageAdded(new FluctlightAdditionalLanguageAddedEvent(fluctlight(DiscordLocale.ENGLISH_US, Set.of(DiscordLocale.FRENCH)), DiscordLocale.FRENCH));

		verify(roles, never()).hold(anyLong(), anyString(), anyBoolean(), anyString());
	}

	@Test
	void cleared_TakesTheRolesOfLanguagesNoLongerSpoken() {
		rule.onCleared(new FluctlightClearedEvent(fluctlight(DiscordLocale.GERMAN, Set.of()), fluctlight(DiscordLocale.ENGLISH_US, Set.of())));

		verify(roles).hold(USER, "100", false, "language de");
		verify(roles).hold(USER, "200", true, "language en-US");
	}

	private Fluctlight fluctlight(DiscordLocale primary, Set<DiscordLocale> additional) {
		return Fluctlight.builder().user(user).primaryLanguage(primary).additionalLanguages(additional).build();
	}

	private static Language language(String role) {
		Language language = new Language();
		language.setRole(role);

		return language;
	}
}
