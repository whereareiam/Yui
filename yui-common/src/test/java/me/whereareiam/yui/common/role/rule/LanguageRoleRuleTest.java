package me.whereareiam.yui.common.role.rule;

import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import me.whereareiam.yui.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.model.config.languages.Language;
import me.whereareiam.yui.model.config.languages.Languages;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LanguageRoleRuleTest {
	private static final String GUILD = "1";

	@Mock
	private JDA jda;

	@Mock
	private Guild guild;

	@Mock
	private LanguagesProvider languagesProvider;

	@Mock
	private ObjectProvider<Settings> settingsProvider;

	@Mock
	private AuditableRestAction<Void> action;

	@Mock
	private Role german;

	@Mock
	private Role english;

	@Mock
	private User user;

	private LanguageRoleRule rule;

	@BeforeEach
	void setUp() {
		when(languagesProvider.get()).thenReturn(new Languages(Map.of(
				DiscordLocale.GERMAN, language("100"),
				DiscordLocale.ENGLISH_US, language("200")
		)));

		Settings settings = new Settings();
		settings.setGuild(GUILD);
		when(settingsProvider.getIfAvailable()).thenReturn(settings);

		when(jda.getGuildById(GUILD)).thenReturn(guild);
		when(guild.getRoleById("100")).thenReturn(german);
		when(guild.getRoleById("200")).thenReturn(english);
		when(guild.addRoleToMember(any(UserSnowflake.class), any(Role.class))).thenReturn(action);
		when(guild.removeRoleFromMember(any(UserSnowflake.class), any(Role.class))).thenReturn(action);
		when(action.reason(anyString())).thenReturn(action);
		when(user.getIdLong()).thenReturn(123L);

		rule = new LanguageRoleRule(jda, languagesProvider, settingsProvider);
	}

	@Test
	void languageChanged_TakesThePreviousRoleAndGivesTheNew() {
		Fluctlight changed = fluctlight(DiscordLocale.GERMAN, Set.of());

		rule.onLanguageChanged(new FluctlightLanguageChangedEvent(changed, DiscordLocale.ENGLISH_US));

		verify(guild).removeRoleFromMember(any(UserSnowflake.class), any(Role.class));
		verify(guild).addRoleToMember(any(UserSnowflake.class), any(Role.class));
	}

	@Test
	void languageChanged_KeepsThePreviousRoleWhenItIsStillSpoken() {
		Fluctlight changed = fluctlight(DiscordLocale.GERMAN, Set.of(DiscordLocale.ENGLISH_US));

		rule.onLanguageChanged(new FluctlightLanguageChangedEvent(changed, DiscordLocale.ENGLISH_US));

		verify(guild, never()).removeRoleFromMember(any(UserSnowflake.class), any(Role.class));
	}

	@Test
	void additionalLanguageAdded_GivesItsRole() {
		Fluctlight changed = fluctlight(DiscordLocale.ENGLISH_US, Set.of(DiscordLocale.GERMAN));

		rule.onAdditionalLanguageAdded(new FluctlightAdditionalLanguageAddedEvent(changed, DiscordLocale.GERMAN));

		verify(guild).addRoleToMember(any(UserSnowflake.class), any(Role.class));
	}

	@Test
	void additionalLanguageRemoved_TakesItsRole() {
		Fluctlight changed = fluctlight(DiscordLocale.ENGLISH_US, Set.of());

		rule.onAdditionalLanguageRemoved(new FluctlightAdditionalLanguageRemovedEvent(changed, DiscordLocale.GERMAN));

		verify(guild).removeRoleFromMember(any(UserSnowflake.class), any(Role.class));
	}

	@Test
	void languageWithoutRole_ChangesNoRole() {
		Fluctlight changed = fluctlight(DiscordLocale.ENGLISH_US, Set.of(DiscordLocale.FRENCH));

		rule.onAdditionalLanguageAdded(new FluctlightAdditionalLanguageAddedEvent(changed, DiscordLocale.FRENCH));

		verify(guild, never()).addRoleToMember(any(UserSnowflake.class), any(Role.class));
	}

	@Test
	void cleared_TakesTheRolesOfLanguagesNoLongerSpoken() {
		Fluctlight previous = fluctlight(DiscordLocale.GERMAN, Set.of());
		Fluctlight cleared = fluctlight(DiscordLocale.ENGLISH_US, Set.of());

		rule.onCleared(new FluctlightClearedEvent(previous, cleared));

		verify(guild).removeRoleFromMember(any(UserSnowflake.class), any(Role.class));
		verify(guild).addRoleToMember(any(UserSnowflake.class), any(Role.class));
	}

	@Test
	void withoutGuild_ChangesNoRole() {
		when(jda.getGuildById(GUILD)).thenReturn(null);

		rule.grant(fluctlight(DiscordLocale.GERMAN, Set.of(DiscordLocale.ENGLISH_US)));

		verify(guild, never()).addRoleToMember(any(UserSnowflake.class), any(Role.class));
	}

	private Fluctlight fluctlight(DiscordLocale primary, Set<DiscordLocale> additional) {
		return Fluctlight.builder()
				.user(user)
				.primaryLanguage(primary)
				.additionalLanguages(additional)
				.build();
	}

	private static Language language(String role) {
		Language language = new Language();
		language.setRole(role);

		return language;
	}
}
