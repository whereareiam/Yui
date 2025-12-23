package me.whereareiam.yui.common.service.conversation;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.conversation.Conversation;
import me.whereareiam.yui.model.conversation.ConversationConfig;
import me.whereareiam.yui.service.ConversationService;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class DefaultConversationService implements ConversationService, Reloadable {
	private static final int DISCORD_CATEGORY_LIMIT = 50;
	private static final EnumSet<Permission> CHANNEL_PERMISSIONS =
			EnumSet.of(Permission.VIEW_CHANNEL,
					Permission.MESSAGE_SEND,
					Permission.MESSAGE_HISTORY,
					Permission.MESSAGE_EXT_EMOJI,
					Permission.MESSAGE_ATTACH_FILES);

	private final JDA jda;
	private final List<Long> categoryIds;

	private final Map<ConversationKey, Conversation> userConversations = new ConcurrentHashMap<>();
	private final Map<Long, Set<Long>> channelUsers = new ConcurrentHashMap<>();
	private final Set<Conversation> activeConversations = ConcurrentHashMap.newKeySet();
	private final Queue<PendingChannelRequest> pendingRequests = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean drainingQueue = new AtomicBoolean(false);

	private final ScheduledExecutorService scheduler =
			Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "conversation-scheduler");
				t.setDaemon(true);
				return t;
			});

	@Autowired
	public DefaultConversationService(
			JDA jda,
			Settings settings,
			Registry<Reloadable> reloadableRegistry
	) {
		this.jda = jda;
		this.categoryIds = settings.getDiscord()
				.getChannels()
				.getTempChannelCategories()
				.stream()
				.map(Long::parseLong)
				.toList();

		reloadableRegistry.register(this);
	}

	public CompletableFuture<Void> purgeChannels() {
		CompletableFuture<Void> result = new CompletableFuture<>();
		Guild guild = jda.getGuilds().getFirst();

		List<GuildChannel> channelsToDelete = new ArrayList<>();
		for (long cid : categoryIds) {
			Category cat = guild.getCategoryById(cid);
			if (cat == null) continue;
			channelsToDelete.addAll(cat.getChannels());
		}

		if (channelsToDelete.isEmpty())
			return CompletableFuture.completedFuture(null);

		log.info("[ConversationService]: Queuing {} channels for purge", channelsToDelete.size());

		AtomicInteger remaining = new AtomicInteger(channelsToDelete.size());
		for (int i = 0; i < channelsToDelete.size(); i++) {
			final int index = i;
			scheduler.schedule(() -> channelsToDelete.get(index)
					.delete()
					.reason("Yui restart – removing stale temp channel")
					.queue(
							_ -> {
								if (remaining.decrementAndGet() == 0) {
									log.info("[ConversationService]: Startup channel purge completed");
									result.complete(null);
								}
							},
							ex -> {
								log.warn("Failed to delete channel {}: {}",
										channelsToDelete.get(index).getName(), ex.getMessage());
								if (remaining.decrementAndGet() == 0) {
									result.complete(null);
								}
							}
					), i * 1000L, TimeUnit.MILLISECONDS);
		}

		return result;
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
		CompletableFuture<Conversation> future = new CompletableFuture<>();

		jda.retrieveUserById(userId).queue(
				user -> user.openPrivateChannel().queue(
						channel -> testPrivateMessageAccess(user, channel, context, config, future),
						ex -> {
							log.debug("Failed to open PM for user {}: {}", userId, ex.getMessage());
							fallbackToChannel(userId, context, config, future);
						}
				),
				ex -> {
					log.warn("Failed to retrieve user {}: {}", userId, ex.getMessage());
					future.completeExceptionally(ex);
				}
		);

		return future;
	}

	private void testPrivateMessageAccess(User user, PrivateChannel channel, String context, ConversationConfig config, CompletableFuture<Conversation> future) {
		String pmMessage = pmInitialMessage(config);

		if (pmMessage != null && !pmMessage.isBlank()) {
			channel.sendMessage(pmMessage).queue(
					_ -> {
						Conversation conversation = new PrivateMessageConversation(user, channel);
						registerConversation(conversation, context);
						future.complete(conversation);
					},
					ex -> {
						log.debug("Cannot send PM to user {}: {}", user.getIdLong(), ex.getMessage());
						fallbackToChannel(user.getIdLong(), context, config, future);
					}
			);
		} else {
			Conversation conversation = new PrivateMessageConversation(user, channel);
			registerConversation(conversation, context);
			future.complete(conversation);
		}
	}

	private void fallbackToChannel(long userId, String context, ConversationConfig config, CompletableFuture<Conversation> future) {
		if (config.isAllowTemporaryChannel()) {
			createTemporaryChannel(Collections.singleton(userId), context, config)
					.whenComplete((conv, ex) -> {
						if (ex == null)
							future.complete(conv);
						else
							future.completeExceptionally(ex);
					});
		} else {
			future.completeExceptionally(
					new IllegalStateException("Cannot create PM and temp channels are disabled"));
		}
	}

	private CompletableFuture<Conversation> createTemporaryChannel(Collection<Long> userIds, String context, ConversationConfig config) {
		CompletableFuture<Conversation> result = new CompletableFuture<>();
		Guild guild = jda.getGuilds().getFirst();

		String channelName = config.getChannelName() != null ? config.getChannelName() : "#" + new Random().nextInt();
		tryCreateChannelRecursive(guild, userIds, context, config, channelName, 0, result);
		return result;
	}

	private void tryCreateChannelRecursive(Guild guild, Collection<Long> userIds, String context, ConversationConfig config, String channelName, int index, CompletableFuture<Conversation> result) {
		if (index >= categoryIds.size()) {
			pendingRequests.add(new PendingChannelRequest(userIds, context, config, channelName, result));
			log.info("[ConversationService]: All temporary-channel categories are full – queued a request for {} user(s)", userIds.size());
			return;
		}

		Long cid = categoryIds.get(index);
		Category category = guild.getCategoryById(cid);
		if (category == null || category.getChannels().size() >= DISCORD_CATEGORY_LIMIT) {
			tryCreateChannelRecursive(guild, userIds, context, config, channelName, index + 1, result);
			return;
		}

		ChannelAction<TextChannel> action = category
				.createTextChannel(channelName)
				.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));

		if (config.getChannelDescription() != null && !config.getChannelDescription().isEmpty())
			action = action.setTopic(config.getChannelDescription());

		for (long uid : userIds) {
			Member member = guild.retrieveMemberById(uid).complete();
			if (member != null)
				action = action.addPermissionOverride(member, CHANNEL_PERMISSIONS, null);
		}

		action.queue(
				channel -> {
					channelUsers.put(channel.getIdLong(), new HashSet<>(userIds));

					String channelMessage = channelInitialMessage(config);

					if (config.isMentionUsers() || (channelMessage != null && !channelMessage.isBlank())) {
						StringBuilder sb = new StringBuilder();
						if (config.isMentionUsers())
							userIds.forEach(id -> sb.append("<@").append(id).append("> "));
						if (channelMessage != null && !channelMessage.isBlank())
							sb.append(channelMessage);
						channel.sendMessage(sb.toString().trim()).queue();
					}

					Conversation conversation = new TemporaryChannelConversation(new HashSet<>(userIds), channel, this);
					registerConversation(conversation, context);

					result.complete(conversation);
				},
				failure -> {
					boolean categoryFull = failure instanceof ErrorResponseException ex
							&& ex.getErrorCode() == 50035
							&& ex.getMessage() != null
							&& ex.getMessage().contains("CHANNEL_PARENT_MAX_CHANNELS");

					if (categoryFull) {
						tryCreateChannelRecursive(guild, userIds, context, config, channelName, index + 1, result);
						return;
					}

					result.completeExceptionally(failure);
				});
	}

	CompletableFuture<Void> closeChannel(TextChannel channel, long delaySeconds) {
		if (channel == null)
			return CompletableFuture.completedFuture(null);

		if (delaySeconds <= 0) {
			CompletableFuture<Void> future = new CompletableFuture<>();
			channel.delete()
					.reason("Closed via ConversationService")
					.queue(_ -> future.complete(null), future::completeExceptionally);
			return future;
		}

		CompletableFuture<Void> future = new CompletableFuture<>();
		Set<Long> userIds = channelUsers.get(channel.getIdLong());
		Long firstUserId = (userIds != null && !userIds.isEmpty()) ? userIds.iterator().next() : null;

		EmbedBuilder builder = StyleKit.embeds().warning();
		if (firstUserId != null) {
			builder.setTitle(Translatable.text("general.conversation.close.title").resolve(firstUserId));
			builder.setDescription(
					Translatable.text("general.conversation.close.description")
							.with("seconds", delaySeconds)
							.resolve(firstUserId));
		} else {
			builder.setTitle(Translatable.text("general.conversation.close.title").resolveDefault());
			builder.setDescription(
					Translatable.text("general.conversation.close.description")
							.with("seconds", delaySeconds)
							.resolveDefault());
		}

		channel.sendMessageEmbeds(builder.build())
				.queue(
						_ -> scheduler.schedule(() ->
										closeChannel(channel, 0)
												.whenComplete((_, ex) -> {
													if (ex == null)
														future.complete(null);
													else
														future.completeExceptionally(ex);
												}),
								delaySeconds, TimeUnit.SECONDS),
						future::completeExceptionally);

		return future;
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
		channelUsers.clear();
	}

	public void handleChannelDeletion(long channelId) {
		Set<Long> users = channelUsers.remove(channelId);
		if (users != null) {
			userConversations.entrySet().removeIf(entry -> 
				users.contains(entry.getKey().userId()) &&
				entry.getValue().getChannel() instanceof TextChannel &&
				entry.getValue().getChannel().getIdLong() == channelId
			);
		}

		activeConversations.stream()
				.filter(conv -> conv.getChannel() instanceof TextChannel)
				.filter(conv -> conv.getChannel().getIdLong() == channelId)
				.findFirst()
				.ifPresent(activeConversations::remove);

		drainQueue();
	}

	private void drainQueue() {
		if (!drainingQueue.compareAndSet(false, true)) return;

		scheduler.execute(() -> {
			try {
				PendingChannelRequest req;
				while ((req = pendingRequests.peek()) != null) {
					Guild guild = jda.getGuilds().getFirst();

					CompletableFuture<Conversation> marker = new CompletableFuture<>();
					tryCreateChannelRecursive(guild, req.userIds(), req.context(), req.config(), req.channelName(), 0, marker);

					PendingChannelRequest finalReq = req;
					marker.whenComplete((conv, ex) -> {
						if (ex == null) {
							pendingRequests.poll();
							finalReq.future().complete(conv);
						} else {
							drainingQueue.set(false);
						}
					});

					if (!marker.isDone()) break;
				}
			} finally {
				drainingQueue.set(false);
			}
		});
	}

	public void handleUserLeave(long userId) {
		userConversations.entrySet().stream()
				.filter(entry -> entry.getKey().userId() == userId)
				.map(Map.Entry::getValue)
				.distinct()
				.forEach(this::close);
	}

	private record ConversationKey(long userId, String context) {}

	private record PendingChannelRequest(
			Collection<Long> userIds,
			String context,
			ConversationConfig config,
			String channelName,
			CompletableFuture<Conversation> future
	) {}
}
