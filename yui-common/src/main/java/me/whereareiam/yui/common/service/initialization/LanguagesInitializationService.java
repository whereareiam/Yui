package me.whereareiam.yui.common.service.initialization;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.model.config.Roles;
import me.whereareiam.yui.service.LanguageService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class LanguagesInitializationService {
	private final ObjectProvider<Roles> rolesProvider;
	private final LanguageService languageService;

	@Order(Integer.MIN_VALUE)
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		List<DiscordLocale> languageRoles = rolesProvider.getObject().getLanguageRoles().keySet().stream()
				.map(DiscordLocale::from)
				.toList();

		removeObsoleteLanguages(languageRoles);
		addMissingLanguages(languageRoles);
	}

	private void removeObsoleteLanguages(List<DiscordLocale> languageRoles) {
		Set<DiscordLocale> languageRolesSet = Set.copyOf(languageRoles);
		languageService.getAvailableLanguages().stream()
				.filter(lang -> !languageRolesSet.contains(lang))
				.forEach(lang -> {
					log.info("Removing obsolete language: {}", lang);
					languageService.removeLanguage(lang);
				});
	}

	private void addMissingLanguages(List<DiscordLocale> languageRoles) {
		Set<DiscordLocale> availableLanguagesSet = new HashSet<>(languageService.getAvailableLanguages());
		languageRoles.stream()
				.filter(lang -> !availableLanguagesSet.contains(lang))
				.forEach(lang -> {
					log.info("Adding missing language: {}", lang);
					languageService.addLanguage(lang);
				});
	}
}
