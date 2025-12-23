package me.whereareiam.yui.common.listener.fluctlight;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.common.config.provider.LanguagesProvider;
import me.whereareiam.yui.common.config.provider.RolesProvider;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.model.config.languages.LanguageEntry;
import me.whereareiam.yui.model.config.languages.Languages;
import me.whereareiam.yui.model.config.roles.RoleEntry;
import me.whereareiam.yui.service.RoleService;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Listener that triggers role synchronization when language settings change.
 * Language changes may affect which roles a user should have based on language-specific roles.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FluctlightLanguageChangeListener {
	private final RoleService roleService;
	private final LanguagesProvider languagesProvider;
	private final RolesProvider rolesProvider;

	@Order(1)
	@EventListener
	public void onLanguageChanged(FluctlightLanguageChangedEvent event) {
		// Remove role tied to the old primary language (if any)
		resolveRoleId(event.getOldLanguage())
				.ifPresent(roleId -> event.getFluctlight().removeAllowedRole(roleId));

		// Add role tied to the new primary language (if any)
		resolveRoleId(event.getNewLanguage())
				.ifPresent(roleId -> event.getFluctlight().addAllowedRole(roleId));

		roleService.syncUserRoles(event.getFluctlight());
		log.debug("Triggered role sync for user {} after primary language change", event.getFluctlight().getId());
	}

	@Order(1)
	@EventListener
	public void onAdditionalLanguageAdded(FluctlightAdditionalLanguageAddedEvent event) {
		if (event.isCancelled())
			return;

		resolveRoleId(event.getLanguage())
				.ifPresent(roleId -> event.getFluctlight().addAllowedRole(roleId));

		roleService.syncUserRoles(event.getFluctlight());
		log.debug("Triggered role sync for user {} after additional language added", event.getFluctlight().getId());
	}

	@Order(1)
	@EventListener
	public void onAdditionalLanguageRemoved(FluctlightAdditionalLanguageRemovedEvent event) {
		if (event.isCancelled())
			return;

		resolveRoleId(event.getLanguage())
				.ifPresent(roleId -> event.getFluctlight().removeAllowedRole(roleId));

		roleService.syncUserRoles(event.getFluctlight());
		log.debug("Triggered role sync for user {} after additional language removed", event.getFluctlight().getId());
	}

	/**
	 * Resolve the role id for a given locale using only the explicit role mapping
	 * defined on the language entry in languages.yml.
	 */
	private Optional<Long> resolveRoleId(DiscordLocale locale) {
		if (locale == null)
			return Optional.empty();

		Languages languages = languagesProvider.get();
		LanguageEntry entry = languages.toLocaleMap().get(locale);
		Long roleId = entry != null ? entry.getRole() : null;

		if (roleId == null || roleId == 0)
			return Optional.empty();

		if (!roleService.isRoleAllowed(roleId))
			return Optional.empty();

		// Ensure the role exists in roles.yml (or API-added) for sanity
		Optional<RoleEntry> roleEntry = rolesProvider.get().getRoles().stream()
				.filter(r -> r != null && r.getId() == roleId)
				.findFirst();

		return roleEntry.map(RoleEntry::getId);
	}
}
