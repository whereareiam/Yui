package me.whereareiam.yui.common.journey.component;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.event.journey.session.JourneySessionRemovedEvent;
import me.whereareiam.yui.journey.JourneyKeys;
import me.whereareiam.yui.service.InteractionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JourneyComponentBindingCleanupListener {
	private final @NotNull InteractionService interactions;

	@EventListener
	public void onSessionRemoved(@NotNull JourneySessionRemovedEvent event) {
		String sessionId = event.getSession().getId();
		interactions.unbindAllByAttribute(JourneyKeys.SESSION_ID, sessionId);
	}
}
