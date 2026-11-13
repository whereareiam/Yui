package me.whereareiam.yui.common.config.provider;

import me.whereareiam.yui.api.model.config.conversation.Conversations;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.common.config.template.ConversationsTemplate;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reads {@code conversations.yml}. A thread's {@code archive} must be one of Discord's: 1h, 24h, 3d or 1w.
 */
@Component
public class ConversationsProvider extends DefaultConfigProvider<Conversations> {
	@Autowired
	private ConversationsTemplate template;

	@Override
	protected Conversations load() {
		Conversations conversations = read("conversations", template.supply(new Conversations()));
		Duration archive = conversations.getTypes().getThread().getArchive();
		try {
			ThreadChannel.AutoArchiveDuration.fromKey((int) (archive.getSeconds() / 60));
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("conversations.yml: types.thread.archive is 1h, 24h, 3d or 1w, not " + archive, exception);
		}

		return conversations;
	}

	@Override
	public Class<Conversations> getObjectType() {
		return Conversations.class;
	}
}
