package me.whereareiam.yui.common.conversation;

import me.whereareiam.yui.api.exception.ConversationRefusedException.Reason;
import me.whereareiam.yui.api.model.config.conversation.Conversations;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The conversations Yui has open, and when each member's last one of a purpose closed: what the limits of a purpose
 * are checked against.
 */
@Component
public class ConversationLedger {
	private final Set<OpenConversation> open = ConcurrentHashMap.newKeySet();
	private final Map<Key, Instant> closed = new ConcurrentHashMap<>();
	private final Clock clock;

	public ConversationLedger() {
		this(Clock.systemUTC());
	}

	ConversationLedger(Clock clock) {
		this.clock = clock;
	}

	Optional<OpenConversation> find(long member, String purpose) {
		return open.stream()
				.filter(conversation -> conversation.getPurpose().equals(purpose))
				.filter(conversation -> conversation.getMembers().contains(member))
				.findFirst();
	}

	Optional<OpenConversation> byChannel(long channel) {
		return open.stream().filter(conversation -> conversation.getChannel().getIdLong() == channel).findFirst();
	}

	List<OpenConversation> of(long member) {
		return open.stream().filter(conversation -> conversation.getMembers().contains(member)).toList();
	}

	List<OpenConversation> all() {
		return List.copyOf(open);
	}

	/**
	 * @return why a new conversation of the purpose may not open for one of the members, if it may not
	 */
	Optional<Reason> refusal(Collection<Long> members, String purpose, Conversations.Limit limit) {
		for (long member : members) {
			long count = open.stream()
					.filter(conversation -> conversation.getPurpose().equals(purpose))
					.filter(conversation -> conversation.getMembers().contains(member))
					.count();
			if (limit.getOpen() > 0 && count >= limit.getOpen()) return Optional.of(Reason.LIMIT);

			Instant last = closed.get(new Key(member, purpose));
			if (last != null && limit.getCooldown() != null
					&& last.plusSeconds(limit.getCooldown().getSeconds()).isAfter(clock.instant()))
				return Optional.of(Reason.COOLDOWN);
		}

		return Optional.empty();
	}

	void add(OpenConversation conversation) {
		open.add(conversation);
	}

	void remove(OpenConversation conversation) {
		if (!open.remove(conversation)) return;

		Instant now = clock.instant();
		conversation.getMembers().forEach(member -> closed.put(new Key(member, conversation.getPurpose()), now));
	}

	private record Key(long member, String purpose) {
	}
}
