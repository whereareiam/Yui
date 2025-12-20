package me.whereareiam.yui.common.listener.fluctlight;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.fluctlight.FluctlightUpdatedEvent;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener that updates the in-memory Fluctlight object when data is updated in the database.
 * This keeps persistence operations separate from in-memory state management.
 */
@Slf4j
@Component
public class FluctlightUpdatedListener {
	@Order(0)
	@EventListener
	public void onFluctlightDataUpdated(FluctlightUpdatedEvent event) {
		Fluctlight fluctlight = event.getFluctlight();
		var data = event.getData();

		// Update in-memory Fluctlight object with persisted data
		fluctlight.setPrimaryLanguageInternal(data.getPrimaryLanguage());
		fluctlight.setAdditionalLanguagesInternal(data.getAdditionalLanguages());
		fluctlight.setAllowedRolesInternal(data.getAllowedRoles());

		log.trace("Updated in-memory Fluctlight {} with persisted data", fluctlight.getId());
	}
}
