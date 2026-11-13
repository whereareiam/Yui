package me.whereareiam.yui.interests.listener;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.interests.service.TopicPicks;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * A cleared profile ({@code /clear}) forgets its picks and their roles.
 */
@Component
@RequiredArgsConstructor
public class ProfileListener {
	private final TopicPicks picks;

	@TransactionalEventListener
	public void onCleared(FluctlightClearedEvent event) {
		picks.forget(event.getFluctlight().getId());
	}
}
