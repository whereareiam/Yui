package me.whereareiam.yui.common.listener.fluctlight;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.fluctlight.FluctlightCreatedEvent;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightStateUpdater;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener that initializes the in-memory Fluctlight object when it is created.
 * This loads the persisted data and applies it to the in-memory object.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FluctlightCreatedListener {
	private final FluctlightPersistence fluctlightPersistence;

	@Order(0)
	@EventListener
	public void onFluctlightCreated(FluctlightCreatedEvent event) {
		Fluctlight fluctlight = event.getFluctlight();

		// Load persisted data and apply to in-memory object
		fluctlightPersistence.loadData(fluctlight).ifPresent(data -> {
			FluctlightStateUpdater.updatePrimaryLanguage(fluctlight, data.getPrimaryLanguage());
			FluctlightStateUpdater.updateAdditionalLanguages(fluctlight, data.getAdditionalLanguages());
			FluctlightStateUpdater.updateAllowedRoles(fluctlight, data.getAllowedRoles());
			log.trace("Initialized in-memory Fluctlight {} with persisted data", fluctlight.getId());
		});
	}
}
