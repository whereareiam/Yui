package me.whereareiam.yui.common.service.conversation;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.common.service.conversation.type.pm.PrivateMessageConversationCreator;
import me.whereareiam.yui.common.service.conversation.type.channel.TempChannelConversationCreator;
import me.whereareiam.yui.model.conversation.Conversation;
import me.whereareiam.yui.model.conversation.ConversationConfig;
import me.whereareiam.yui.service.ConversationService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DefaultConversationService extends ListenerAdapter implements ConversationService, Reloadable {
	private final Map<ConversationKey, Conversation> userConversations = new ConcurrentHashMap<>();
	private final Set<Conversation> activeConversations = ConcurrentHashMap.newKeySet();
	private final PrivateMessageConversationCreator privateMessageConversationCreator;
	private final TempChannelConversationCreator tempChannelConversationCreator;

	@Autowired
	public DefaultConversationService(
			PrivateMessageConversationCreator privateMessageConversationCreator,
			TempChannelConversationCreator tempChannelConversationCreator,
			Registry<Reloadable> reloadableRegistry
	) {
		this.privateMessageConversationCreator = privateMessageConversationCreator;
		this.tempChannelConversationCreator = tempChannelConversationCreator;

		reloadableRegistry.register(this);
	}

	public CompletableFuture<Void> purgeChannels() {
		return tempChannelConversationCreator.purgeChannels();
	}

	@Override
	public CompletableFuture<Conversation> create(long userId, String context) {
		return create(userId, context, ConversationConfig.builder().build());
	}

	@Override
	public CompletableFuture<Conversation> create(long userId, String context, ConversationConfig config) {
		return create(Collections.singleton(userId), context, config);
	}

	@Override
	public CompletableFuture<Conversation> create(Collection<Long> userIds, String context) {
		return create(userIds, context, ConversationConfig.builder().build());
	}

	@Override
	public CompletableFuture<Conversation> create(Collection<Long> userIds, String context, ConversationConfig config) {
		if (userIds.isEmpty())
			return CompletableFuture.failedFuture(new IllegalArgumentException("No users specified"));
		if (context == null || context.isBlank())
			return CompletableFuture.failedFuture(new IllegalArgumentException("Context cannot be null or empty"));

		long firstUserId = userIds.iterator().next();
		Optional<Conversation> existing = findByUser(firstUserId, context);
		if (existing.isPresent() && existing.get().isActive())
			return CompletableFuture.completedFuture(existing.get());

		if (userIds.size() == 1 && config.isPreferPrivateMessage())
			return tryCreatePrivateMessage(firstUserId, context, config);

		if (config.isAllowTemporaryChannel())
			return createTemporaryChannel(userIds, context, config);

		if (userIds.size() == 1)
			return tryCreatePrivateMessage(firstUserId, context, config);

		return CompletableFuture.failedFuture(
				new IllegalStateException("Cannot create conversation: PM not possible and temp channels disabled"));
	}

	private CompletableFuture<Conversation> tryCreatePrivateMessage(long userId, String context, ConversationConfig config) {
		String pmMessage = pmInitialMessage(config);
		return privateMessageConversationCreator.create(userId, pmMessage)
				.thenApply(conv -> {
					registerConversation(conv, context);
					return conv;
				})
				.handle((conv, ex) -> {
					if (ex == null)
						return CompletableFuture.completedFuture(conv);

					if (config.isAllowTemporaryChannel())
						return createTemporaryChannel(Collections.singleton(userId), context, config);

					return CompletableFuture.<Conversation>failedFuture(ex);
				})
				.thenCompose(future -> future);
	}

	private CompletableFuture<Conversation> createTemporaryChannel(Collection<Long> userIds, String context, ConversationConfig config) {
		String channelMessage = channelInitialMessage(config);
		return tempChannelConversationCreator.create(userIds, config, channelMessage)
				.thenApply(conv -> {
					registerConversation(conv, context);
					return conv;
				});
	}

	private void registerConversation(Conversation conversation, String context) {
		activeConversations.add(conversation);
		conversation.getParticipants().forEach(id -> userConversations.put(new ConversationKey(id, context), conversation));
	}

	@Override
	public Optional<Conversation> findByUser(long userId, String context) {
		ConversationKey key = new ConversationKey(userId, context);
		Conversation conversation = userConversations.get(key);
		if (conversation != null && conversation.isActive())
			return Optional.of(conversation);

		userConversations.remove(key);
		return Optional.empty();
	}

	@Override
	public CompletableFuture<Void> close(Conversation conversation) {
		return closeInternal(conversation, 0);
	}

	@Override
	public CompletableFuture<Void> close(Conversation conversation, long delaySeconds) {
		return closeInternal(conversation, delaySeconds);
	}

	private CompletableFuture<Void> closeInternal(Conversation conversation, long delaySeconds) {
		if (conversation == null)
			return CompletableFuture.completedFuture(null);

		return conversation.close(delaySeconds).whenComplete((_, _) -> {
			activeConversations.remove(conversation);
			userConversations.values().removeIf(conv -> conv == conversation);
		});
	}

	private String pmInitialMessage(ConversationConfig config) {
		if (config.getPrivateInitialMessage() != null && !config.getPrivateInitialMessage().isBlank())
			return config.getPrivateInitialMessage();

		return config.getInitialMessage();
	}

	private String channelInitialMessage(ConversationConfig config) {
		if (config.getChannelInitialMessage() != null && !config.getChannelInitialMessage().isBlank())
			return config.getChannelInitialMessage();

		return config.getInitialMessage();
	}

	@Override
	public void reload() {
		log.info("[ConversationService]: Closing {} active conversations", activeConversations.size());
		activeConversations.forEach(conv -> {
			try {
				close(conv).join();
			} catch (Exception ex) {
				log.warn("[ConversationService]: Failed to close conversation", ex);
			}
		});
		activeConversations.clear();
		userConversations.clear();
	}

	public void handleChannelDeletion(long channelId) {
		tempChannelConversationCreator.handleChannelDeletion(channelId);

		activeConversations.stream()
				.filter(conv -> conv.getChannel() instanceof TextChannel)
				.filter(conv -> conv.getChannel().getIdLong() == channelId)
				.findFirst()
				.ifPresent(conv -> {
					activeConversations.remove(conv);
					userConversations.values().removeIf(existing -> existing == conv);
				});
	}

	@Override
	public void onGuildMemberRemove(@NonNull GuildMemberRemoveEvent event) {
		userConversations.entrySet().stream()
				.filter(entry -> entry.getKey().userId() == event.getUser().getIdLong())
				.map(Map.Entry::getValue)
				.distinct()
				.forEach(this::close);
	}

	private record ConversationKey(long userId, String context) {}
}
