package me.whereareiam.yui.common.role.rule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.api.model.config.languages.Language;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.role.FactRoles;
import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import net.dv8tion.jda.api.interactions.DiscordLocale;
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
	private final FactRoles roles;
	private final LanguagesProvider languagesProvider;

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
		for (DiscordLocale language : languages)
			role(language).ifPresent(role -> roles.hold(fluctlight.getId(), role, fluctlight.speaks(language), "language " + language.getLocale()));
	}

	private Optional<String> role(DiscordLocale language) {
		return languagesProvider.get().get(language)
				.map(Language::getRole)
				.filter(role -> !role.isBlank());
	}
}
