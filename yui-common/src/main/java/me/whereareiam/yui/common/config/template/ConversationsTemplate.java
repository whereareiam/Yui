package me.whereareiam.yui.common.config.template;

import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.yui.api.model.config.conversation.Conversations;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.api.type.ConversationType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The values a {@code conversations.yml} setting takes when the file does not name it. Every type is allowed; a
 * thread needs a {@code parent} and a channel {@code categories} before it can open.
 */
@Component
public class ConversationsTemplate implements DefaultsProvider<Conversations> {
	@Override
	public Conversations supply(Conversations conversations) {
		conversations.setTypes(types());
		conversations.setDefaults(defaults());

		return conversations;
	}

	private static Conversations.Types types() {
		Conversations.Direct direct = new Conversations.Direct();
		direct.setEnabled(true);

		Conversations.Thread thread = new Conversations.Thread();
		thread.setEnabled(true);
		thread.setArchive(Duration.parse("1h"));

		Conversations.Channel channel = new Conversations.Channel();
		channel.setEnabled(true);
		channel.setCategories(List.of());
		channel.setPurge(true);

		Conversations.Types types = new Conversations.Types();
		types.setDirect(direct);
		types.setThread(thread);
		types.setChannel(channel);

		return types;
	}

	private static Conversations.Defaults defaults() {
		Conversations.Close close = new Conversations.Close();
		close.setIdle(Duration.parse("15m"));
		close.setAfter(Duration.parse("2h"));
		close.setWarn(Duration.parse("30s"));

		Conversations.Limit limit = new Conversations.Limit();
		limit.setOpen(1);
		limit.setCooldown(Duration.parse("10m"));

		Conversations.Transcript transcript = new Conversations.Transcript();
		transcript.setAudit(true);

		Conversations.Defaults defaults = new Conversations.Defaults();
		defaults.setVia(List.of(ConversationType.DIRECT, ConversationType.THREAD, ConversationType.CHANNEL));
		defaults.setMention(true);
		defaults.setStaff(List.of());
		defaults.setClose(close);
		defaults.setLimit(limit);
		defaults.setTranscript(transcript);

		return defaults;
	}
}
