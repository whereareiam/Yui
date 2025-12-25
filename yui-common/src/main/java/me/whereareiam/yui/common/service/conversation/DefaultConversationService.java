package me.whereareiam.yui.common.service.conversation;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.common.service.conversation.type.pm.PrivateMessageConversationCreator;
import me.whereareiam.yui.common.service.conversation.type.channel.TempChannelConversationCreator;
import me.whereareiam.yui.conversation.Conversation;
import me.whereareiam.yui.model.ConversationConfig;
import me.whereareiam.yui.model.ConversationMode;
import me.whereareiam.yui.conversation.ConversationService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

		List<ConversationMode> modes = resolveConversationModes(config, userIds.size());
		return tryModesInOrder(userIds, context, config, modes, 0);
	}

	/**
	 * Resolves the conversation modes to try based on configuration.
	 * Filters out modes that are incompatible with the number of users.
	 */
	private List<ConversationMode> resolveConversationModes(ConversationConfig config, int userCount) {
		if (config.getPreferredModes() == null || config.getPreferredModes().isEmpty()) {
			return new ArrayList<>(List.of(ConversationMode.TEMPORARY_CHANNEL));
		}

		// Filter out PRIVATE_MESSAGE if multiple users (PM only supports single user)
		if (userCount > 1) {
			return config.getPreferredModes().stream()
					.filter(mode -> mode != ConversationMode.PRIVATE_MESSAGE)
					.collect(Collectors.toList());
		}

		return new ArrayList<>(config.getPreferredModes());
	}

	/**
	 * Tries conversation modes in order until one succeeds.
	 */
	private CompletableFuture<Conversation> tryModesInOrder(
			Collection<Long> userIds,
			String context,
			ConversationConfig config,
			List<ConversationMode> modes,
			int modeIndex) {

		if (modeIndex >= modes.size()) {
			return CompletableFuture.failedFuture(
					new IllegalStateException("Cannot create conversation: all modes failed or unavailable"));
		}

		ConversationMode mode = modes.get(modeIndex);

		return tryMode(mode, userIds, context, config)
				.handle((conv, ex) -> {
					if (ex == null)
						return CompletableFuture.completedFuture(conv);

					// Try next mode
					log.debug("Conversation mode {} failed, trying next mode", mode, ex);
					return tryModesInOrder(userIds, context, config, modes, modeIndex + 1);
				})
				.thenCompose(future -> future);
	}

	/**
	 * Attempts to create a conversation using the specified mode.
	 */
	private CompletableFuture<Conversation> tryMode(
			ConversationMode mode,
			Collection<Long> userIds,
			String context,
			ConversationConfig config) {

		return switch (mode) {
			case PRIVATE_MESSAGE -> {
				if (userIds.size() != 1) {
					yield CompletableFuture.failedFuture(
							new IllegalStateException("Private messages only support single user"));
				}
				yield tryCreatePrivateMessage(userIds.iterator().next(), context, config);
			}
			case TEMPORARY_CHANNEL -> createTemporaryChannel(userIds, context, config);
		};
	}

	private CompletableFuture<Conversation> tryCreatePrivateMessage(long userId, String context, ConversationConfig config) {
		String pmMessage = pmInitialMessage(config);
		return privateMessageConversationCreator.create(userId, pmMessage)
				.thenApply(conv -> {
					registerConversation(conv, context);
					return conv;
				});
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
