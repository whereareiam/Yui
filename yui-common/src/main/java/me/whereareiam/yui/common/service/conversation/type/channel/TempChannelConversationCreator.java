package me.whereareiam.yui.common.service.conversation.type.channel;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.conversation.Conversation;
import me.whereareiam.yui.model.conversation.ConversationConfig;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
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

/**
 * Handles creation and lifecycle of temporary channel conversations.
 */
@Slf4j
@Component
public class TempChannelConversationCreator implements Reloadable {
	private static final int DISCORD_CATEGORY_LIMIT = 50;
	private static final EnumSet<Permission> CHANNEL_PERMISSIONS =
			EnumSet.of(Permission.VIEW_CHANNEL,
					Permission.MESSAGE_SEND,
					Permission.MESSAGE_HISTORY,
					Permission.MESSAGE_EXT_EMOJI,
					Permission.MESSAGE_ATTACH_FILES);

	private final JDA jda;
	private final List<Long> categoryIds;

	private final Map<Long, Set<Long>> channelUsers = new ConcurrentHashMap<>();
	private final Queue<PendingChannelRequest> pendingRequests = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean drainingQueue = new AtomicBoolean(false);
	private final ScheduledExecutorService scheduler =
			Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "conversation-channel-scheduler");
				t.setDaemon(true);
				return t;
			});

	@Autowired
	public TempChannelConversationCreator(JDA jda, Settings settings, Registry<Reloadable> reloadableRegistry) {
		this.jda = jda;
		this.categoryIds = settings.getDiscord()
				.getChannels()
				.getTempChannelCategories()
				.stream()
				.map(Long::parseLong)
				.toList();

		reloadableRegistry.register(this);
	}

	public CompletableFuture<Conversation> create(Collection<Long> userIds, ConversationConfig config, String channelMessage) {
		if (userIds.isEmpty())
			return CompletableFuture.failedFuture(new IllegalArgumentException("No users specified"));

		CompletableFuture<Conversation> result = new CompletableFuture<>();
		Guild guild = jda.getGuilds().getFirst();

		String channelName = config.getChannelName() != null ? config.getChannelName() : "#" + new Random().nextInt();
		tryCreateChannelRecursive(guild, userIds, config, channelName, channelMessage, 0, result);
		return result;
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
					.reason("Yui restart - removing stale temp channel")
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

	public CompletableFuture<Void> closeChannel(TextChannel channel, long delaySeconds) {
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

	public void handleChannelDeletion(long channelId) {
		channelUsers.remove(channelId);
		drainQueue();
	}

	@Override
	public void reload() {
		// No-op for now; active channel conversations are managed by DefaultConversationService.
	}

	private void tryCreateChannelRecursive(
			Guild guild,
			Collection<Long> userIds,
			ConversationConfig config,
			String channelName,
			String channelMessage,
			int index,
			CompletableFuture<Conversation> result
	) {
		if (index >= categoryIds.size()) {
			pendingRequests.add(new PendingChannelRequest(userIds, config, channelName, channelMessage, result));
			log.info("[ConversationService]: All temporary-channel categories are full - queued a request for {} user(s)", userIds.size());
			return;
		}

		Long cid = categoryIds.get(index);
		Category category = guild.getCategoryById(cid);
		if (category == null || category.getChannels().size() >= DISCORD_CATEGORY_LIMIT) {
			tryCreateChannelRecursive(guild, userIds, config, channelName, channelMessage, index + 1, result);
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

					if (config.isMentionUsers() || (channelMessage != null && !channelMessage.isBlank())) {
						StringBuilder sb = new StringBuilder();
						if (config.isMentionUsers())
							userIds.forEach(id -> sb.append("<@").append(id).append("> "));
						if (channelMessage != null && !channelMessage.isBlank())
							sb.append(channelMessage);
						channel.sendMessage(sb.toString().trim()).queue();
					}

					Conversation conversation = new TemporaryChannelConversation(new HashSet<>(userIds), channel, this);
					result.complete(conversation);
				},
				failure -> {
					boolean categoryFull = failure instanceof ErrorResponseException ex
							&& ex.getErrorCode() == 50035
							&& ex.getMessage() != null
							&& ex.getMessage().contains("CHANNEL_PARENT_MAX_CHANNELS");

					if (categoryFull) {
						tryCreateChannelRecursive(guild, userIds, config, channelName, channelMessage, index + 1, result);
						return;
					}

					result.completeExceptionally(failure);
				});
	}

	private void drainQueue() {
		if (!drainingQueue.compareAndSet(false, true)) return;

		scheduler.execute(() -> {
			try {
				PendingChannelRequest req;
				while ((req = pendingRequests.peek()) != null) {
					Guild guild = jda.getGuilds().getFirst();

					CompletableFuture<Conversation> marker = new CompletableFuture<>();
					tryCreateChannelRecursive(guild, req.userIds(), req.config(), req.channelName(), req.channelMessage(), 0, marker);

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

	private record PendingChannelRequest(
			Collection<Long> userIds,
			ConversationConfig config,
			String channelName,
			String channelMessage,
			CompletableFuture<Conversation> future
	) {}
}
