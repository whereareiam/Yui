package me.whereareiam.yui.common.role.rule;

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
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Coordinates role assignments based on language selections.
 * <p>
 * This coordinator implements the business rule that certain languages
 * should automatically grant corresponding Discord roles. When a user
 * changes their language preferences, this coordinator updates their
 * roles accordingly.
 * <p>
 * <b>Example:</b> User selects German (DE) → Automatically gets German role
 * <p>
 * This is <b>business logic</b>, not infrastructure. It translates domain events
 * (language changes) into role operations, implementing the language-to-role
 * mapping rules defined in the configuration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LanguageRoleRule {
	private final LanguagesProvider languagesProvider;
	private final RolesProvider rolesProvider;

	/**
	 * Handles primary language changes.
	 * <p>
	 * When a user changes their primary language, this coordinator:
	 * <ul>
	 *   <li>Removes the role associated with the old language (if any)</li>
	 *   <li>Adds the role associated with the new language (if any)</li>
	 * </ul>
	 * The role changes are queued and processed asynchronously by RoleSyncScheduler.
	 *
	 * @param event The language change event
	 */
	@Order(1)
	@EventListener
	public void onLanguageChanged(FluctlightLanguageChangedEvent event) {
		// Remove role tied to the old primary language (if any)
		resolveRoleId(event.getOldLanguage())
				.ifPresent(roleId -> event.getFluctlight().removeAllowedRole(roleId));

		// Add role tied to the new primary language (if any)
		resolveRoleId(event.getNewLanguage())
				.ifPresent(roleId -> event.getFluctlight().addAllowedRole(roleId));

		// No explicit sync needed - RoleSyncScheduler handles it after debounce
		log.debug("Queued role changes for user {} after primary language change", event.getFluctlight().getId());
	}

	/**
	 * Handles additional language additions.
	 * <p>
	 * When a user adds an additional language, this coordinator adds the
	 * corresponding role (if configured). The role change is queued and
	 * processed asynchronously by RoleSyncScheduler.
	 *
	 * @param event The additional language added event
	 */
	@Order(1)
	@EventListener
	public void onAdditionalLanguageAdded(FluctlightAdditionalLanguageAddedEvent event) {
		if (event.isCancelled())
			return;

		resolveRoleId(event.getLanguage())
				.ifPresent(roleId -> event.getFluctlight().addAllowedRole(roleId));

		// No explicit sync needed - RoleSyncScheduler handles it after debounce
		log.debug("Queued role changes for user {} after additional language added", event.getFluctlight().getId());
	}

	/**
	 * Handles additional language removals.
	 * <p>
	 * When a user removes an additional language, this coordinator removes the
	 * corresponding role (if configured). The role change is queued and
	 * processed asynchronously by RoleSyncScheduler.
	 *
	 * @param event The additional language removed event
	 */
	@Order(1)
	@EventListener
	public void onAdditionalLanguageRemoved(FluctlightAdditionalLanguageRemovedEvent event) {
		if (event.isCancelled())
			return;

		resolveRoleId(event.getLanguage())
				.ifPresent(roleId -> event.getFluctlight().removeAllowedRole(roleId));

		// No explicit sync needed - RoleSyncScheduler handles it after debounce
		log.debug("Queued role changes for user {} after additional language removed", event.getFluctlight().getId());
	}

	/**
	 * Resolves the role ID for a given locale.
	 * <p>
	 * This method looks up the explicit role mapping defined in languages.yml
	 * and verifies that the role exists in the roles configuration.
	 *
	 * @param locale The Discord locale to resolve
	 * @return Optional containing the role ID if found and valid, empty otherwise
	 */
	private Optional<Long> resolveRoleId(DiscordLocale locale) {
		if (locale == null)
			return Optional.empty();

		Languages languages = languagesProvider.get();
		LanguageEntry entry = languages.toLocaleMap().get(locale);
		Long roleId = entry != null ? entry.getRole() : null;

		if (roleId == null || roleId == 0)
			return Optional.empty();

		// Verify the role exists in roles.yml (or API-added)
		Optional<RoleEntry> roleEntry = rolesProvider.get().getRoles().stream()
				.filter(r -> r != null && r.getId() == roleId)
				.findFirst();

		return roleEntry.map(RoleEntry::getId);
	}
}
