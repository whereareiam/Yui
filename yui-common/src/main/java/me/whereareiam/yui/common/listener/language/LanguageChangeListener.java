package me.whereareiam.yui.common.listener.language;

import lombok.AllArgsConstructor;
import me.whereareiam.yui.event.language.AdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.language.AdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.language.LanguageChangeEvent;
import me.whereareiam.yui.model.config.Roles;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.service.UserRoleService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LanguageChangeListener {
	private final UserRoleService userRoleService;
	private final ObjectProvider<Roles> roles;

	@Order(0)
	@EventListener
	public void onLanguageChangeEvent(LanguageChangeEvent event) {
		Fluctlight fluctlight = event.getFluctlight();
		DiscordLocale language = event.getFluctlight().getPrimaryLanguage();
		DiscordLocale oldLanguage = event.getOldLanguage();

		if (language == null || (language.equals(oldLanguage)))
			return;

		if (oldLanguage != null)
			userRoleService.removeRoleFromUser(fluctlight.getId(), getRoleId(oldLanguage.getLocale()));

		userRoleService.addRoleToUser(fluctlight.getId(), getRoleId(language.getLocale()));
	}

	@EventListener
	public void onAdditionalLanguageAddedEvent(AdditionalLanguageAddedEvent event) {
		Fluctlight fluctlight = event.getFluctlight();
		DiscordLocale language = event.getLanguage();
		
		if (language != null)
			userRoleService.addRoleToUser(fluctlight.getId(), getRoleId(language.getLocale()));
	}

	@EventListener
	public void onAdditionalLanguageRemovedEvent(AdditionalLanguageRemovedEvent event) {
		Fluctlight fluctlight = event.getFluctlight();
		DiscordLocale language = event.getLanguage();
		
		if (language != null)
			userRoleService.removeRoleFromUser(fluctlight.getId(), getRoleId(language.getLocale()));
	}

	private long getRoleId(String language) {
		return roles.getObject().getLanguageRoles().get(language);
	}
}
