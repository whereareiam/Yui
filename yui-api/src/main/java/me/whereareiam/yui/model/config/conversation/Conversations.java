package me.whereareiam.yui.model.config.conversation;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.type.Duration;
import me.whereareiam.yui.type.ConversationType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * {@code conversations.yml}: what each type of conversation needs and whether this server allows it, and how a
 * conversation behaves when the code that opens it does not say otherwise.
 *
 * <pre>{@code
 * _version: 1
 * types:
 *   thread:
 *     parent: "789..."
 *   channel:
 *     categories: ["123...", "456..."]
 * defaults:
 *   via: [direct, thread, channel]
 *   close:
 *     idle: 15m
 * }</pre>
 */
@Getter
@Setter
public class Conversations {
	private Types types;
	private Defaults defaults;

	/**
	 * Each type's own settings. A type that is not enabled is skipped, for plugins too.
	 */
	@Getter
	@Setter
	public static class Types {
		private Direct direct;
		private Thread thread;
		private Channel channel;
	}

	@Getter
	@Setter
	public static class Direct {
		private boolean enabled;
	}

	/**
	 * Private threads under {@code parent}. Discord archives one after {@code archive} without messages (1h, 24h, 3d
	 * or 1w); {@code invitable} lets members in it add others.
	 */
	@Getter
	@Setter
	public static class Thread {
		private boolean enabled;
		private @Nullable String parent;
		private Duration archive;
		private boolean invitable;
	}

	/**
	 * Temporary channels in {@code categories}, tried in order, 50 channels each; {@code purge} deletes leftovers when
	 * Yui starts.
	 */
	@Getter
	@Setter
	public static class Channel {
		private boolean enabled;
		private List<String> categories;
		private boolean purge;
	}

	/**
	 * How a conversation behaves unless the code that opens it says otherwise.
	 */
	@Getter
	@Setter
	public static class Defaults {
		private List<ConversationType> via;
		private boolean mention;
		private List<String> staff;
		private Close close;
		private Limit limit;
		private Transcript transcript;
	}

	/**
	 * When a conversation closes: after {@code idle} without messages, or {@code after} it opened, with a warning
	 * {@code warn} before. A value left out never closes it that way.
	 */
	@Getter
	@Setter
	public static class Close {
		private @Nullable Duration idle;
		private @Nullable Duration after;
		private @Nullable Duration warn;
	}

	/**
	 * How many conversations of one purpose a member may have open at once, and how long after one closes the next
	 * may open.
	 */
	@Getter
	@Setter
	public static class Limit {
		private int open;
		private @Nullable Duration cooldown;
	}

	/**
	 * Where a closed conversation's messages go: an audit entry {@code conversation.transcript}, and the members.
	 */
	@Getter
	@Setter
	public static class Transcript {
		private boolean audit;
		private boolean member;
	}
}
