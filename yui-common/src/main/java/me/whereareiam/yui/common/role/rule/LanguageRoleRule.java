package me.whereareiam.yui.common.role.rule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import me.whereareiam.yui.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.model.config.languages.LanguageEntry;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Gives a member the role of each language they speak, as {@code languages.yml} assigns it, and takes it away when
 * they stop speaking it. Roles follow this fact one way: a role removed by hand stays removed until the languages
 * change again or the member rejoins.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LanguageRoleRule {
	private final JDA jda;
	private final LanguagesProvider languagesProvider;
	private final ObjectProvider<Settings> settings;

	@TransactionalEventListener
	public void onLanguageChanged(FluctlightLanguageChangedEvent event) {
		Fluctlight fluctlight = event.getFluctlight();
		apply(fluctlight, List.of(event.getPreviousLanguage(), fluctlight.getPrimaryLanguage()));
	}

	@TransactionalEventListener
	public void onAdditionalLanguageAdded(FluctlightAdditionalLanguageAddedEvent event) {
		apply(event.getFluctlight(), List.of(event.getLanguage()));
	}

	@TransactionalEventListener
	public void onAdditionalLanguageRemoved(FluctlightAdditionalLanguageRemovedEvent event) {
		apply(event.getFluctlight(), List.of(event.getLanguage()));
	}

	@TransactionalEventListener
	public void onCleared(FluctlightClearedEvent event) {
		Set<DiscordLocale> languages = new HashSet<>(event.getPrevious().getAdditionalLanguages());
		languages.add(event.getPrevious().getPrimaryLanguage());
		languages.add(event.getFluctlight().getPrimaryLanguage());

		apply(event.getFluctlight(), languages);
	}

	/**
	 * Gives the member the roles of every language they speak, as when they rejoin the server.
	 *
	 * @param fluctlight the member
	 */
	public void grant(Fluctlight fluctlight) {
		Set<DiscordLocale> languages = new HashSet<>(fluctlight.getAdditionalLanguages());
		languages.add(fluctlight.getPrimaryLanguage());

		apply(fluctlight, languages);
	}

	/**
	 * Makes the member hold the role of each given language exactly when they speak it.
	 */
	void apply(Fluctlight fluctlight, Collection<DiscordLocale> languages) {
		Optional<Guild> guild = guild();
		if (guild.isEmpty()) return;

		for (DiscordLocale language : languages)
			role(guild.get(), language).ifPresent(role -> assign(guild.get(), fluctlight, role, fluctlight.speaks(language)));
	}

	private void assign(Guild guild, Fluctlight fluctlight, Role role, boolean speaks) {
		UserSnowflake member = UserSnowflake.fromId(fluctlight.getId());
		var action = speaks ? guild.addRoleToMember(member, role) : guild.removeRoleFromMember(member, role);

		action.reason("Languages of the member changed").queue(
				_ -> log.info("{} role {} for user {}", speaks ? "Gave" : "Took", role.getName(), fluctlight.getId()),
				error -> log.warn("Could not change role {} of user {}: {}", role.getName(), fluctlight.getId(), error.getMessage())
		);
	}

	private Optional<Role> role(Guild guild, DiscordLocale language) {
		LanguageEntry entry = languagesProvider.get().toLocaleMap().get(language);
		if (entry == null || entry.getRole() == null || entry.getRole() == 0) return Optional.empty();

		return Optional.ofNullable(guild.getRoleById(entry.getRole()));
	}

	private Optional<Guild> guild() {
		Settings current = settings.getIfAvailable();
		if (current == null || current.getDiscord() == null || current.getDiscord().getGuildId() == null)
			return Optional.empty();

		return Optional.ofNullable(jda.getGuildById(current.getDiscord().getGuildId()));
	}
}
