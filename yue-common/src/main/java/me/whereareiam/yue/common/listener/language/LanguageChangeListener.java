package me.whereareiam.yue.common.listener.language;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.api.event.language.AdditionalLanguageAddedEvent;
import me.whereareiam.yue.api.event.language.AdditionalLanguageRemovedEvent;
import me.whereareiam.yue.api.event.language.LanguageChangeEvent;
import me.whereareiam.yue.api.input.UserRoleService;
import me.whereareiam.yue.api.model.config.Roles;
import me.whereareiam.yue.api.output.provider.Provider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LanguageChangeListener {
	private final UserRoleService userRoleService;
	private final Provider<Roles> roles;

	@EventListener
	public void onLanguageChangeEvent(LanguageChangeEvent event) {
		if (event.getOldLanguage() != null)
			userRoleService.removeRoleFromUser(event.getUser(), getRoleId(event.getOldLanguage().getLocale()));

		userRoleService.addRoleToUser(event.getUser(), getRoleId(event.getLanguage().getLocale()));
	}

	@EventListener
	public void onAdditionalLanguageAddedEvent(AdditionalLanguageAddedEvent event) {
		if (event.getLanguage() != null)
			userRoleService.addRoleToUser(event.getUser(), getRoleId(event.getLanguage().getLocale()));
	}

	@EventListener
	public void onAdditionalLanguageRemovedEvent(AdditionalLanguageRemovedEvent event) {
		if (event.getLanguage() != null)
			userRoleService.removeRoleFromUser(event.getUser(), getRoleId(event.getLanguage().getLocale()));
	}

	private long getRoleId(String language) {
		return roles.get().getLanguageRoles().get(language);
	}
}