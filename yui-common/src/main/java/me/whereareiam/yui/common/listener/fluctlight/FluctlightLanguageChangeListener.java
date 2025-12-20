package me.whereareiam.yui.common.listener.fluctlight;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightLanguageChangeEvent;
import me.whereareiam.yui.service.RoleService;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FluctlightLanguageChangeListener {
	private final RoleService roleService;

	@Order(0)
	@EventListener
	public void onLanguageChangeEvent(FluctlightLanguageChangeEvent event) {
		if (event.isCancelled())
			return;

		roleService.syncUserRoles(event.getFluctlight());
		log.debug("Triggered role sync for user {} after primary language change", event.getFluctlight().getId());
	}

	@EventListener
	public void onAdditionalLanguageAddedEvent(FluctlightAdditionalLanguageAddedEvent event) {
		if (event.isCancelled())
			return;

		roleService.syncUserRoles(event.getFluctlight());
		log.debug("Triggered role sync for user {} after additional language added", event.getFluctlight().getId());
	}

	@EventListener
	public void onAdditionalLanguageRemovedEvent(FluctlightAdditionalLanguageRemovedEvent event) {
		if (event.isCancelled())
			return;

		roleService.syncUserRoles(event.getFluctlight());
		log.debug("Triggered role sync for user {} after additional language removed", event.getFluctlight().getId());
	}
}
