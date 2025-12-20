package me.whereareiam.yui.common.listener.language;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.event.language.AdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.language.AdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.language.LanguageChangeEvent;
import me.whereareiam.yui.service.UserRoleService;
import me.whereareiam.yui.model.config.Roles;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LanguageChangeListener {
	private final UserRoleService userRoleService;
	private final ObjectProvider<Roles> roles;

	@EventListener
	@Order(0)
	public void onLanguageChangeEvent(LanguageChangeEvent event) {
		if (event.getLanguage().equals(event.getOldLanguage()))
			return;

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
		return roles.getObject().getLanguageRoles().get(language);
	}
}
