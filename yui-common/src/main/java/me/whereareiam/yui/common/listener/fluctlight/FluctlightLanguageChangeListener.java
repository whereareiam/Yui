package me.whereareiam.yui.common.listener.fluctlight;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.service.RoleService;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener that triggers role synchronization when language settings change.
 * Language changes may affect which roles a user should have based on language-specific roles.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FluctlightLanguageChangeListener {
	private final RoleService roleService;

	@Order(1)
	@EventListener
	public void onLanguageChanged(FluctlightLanguageChangedEvent event) {
		roleService.syncUserRoles(event.getFluctlight());
		log.debug("Triggered role sync for user {} after primary language change", event.getFluctlight().getId());
	}

	@Order(1)
	@EventListener
	public void onAdditionalLanguageAdded(FluctlightAdditionalLanguageAddedEvent event) {
		if (event.isCancelled())
			return;

		roleService.syncUserRoles(event.getFluctlight());
		log.debug("Triggered role sync for user {} after additional language added", event.getFluctlight().getId());
	}

	@Order(1)
	@EventListener
	public void onAdditionalLanguageRemoved(FluctlightAdditionalLanguageRemovedEvent event) {
		if (event.isCancelled())
			return;

		roleService.syncUserRoles(event.getFluctlight());
		log.debug("Triggered role sync for user {} after additional language removed", event.getFluctlight().getId());
	}
}
