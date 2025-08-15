package me.whereareiam.yui.common.service;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.input.Registry;
import me.whereareiam.yui.api.input.TemporaryChannelService;
import me.whereareiam.yui.api.model.channel.ChannelDecoration;
import me.whereareiam.yui.api.model.channel.ChannelRequest;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.api.output.Reloadable;
import me.whereareiam.yui.api.style.StyleKit;
import me.whereareiam.yui.api.util.Translatable;
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

@Slf4j
@Component
public class DefaultTemporaryChannelService implements TemporaryChannelService, Reloadable {
	private static final int DISCORD_CATEGORY_LIMIT = 50;

	private final JDA jda;
	private final List<Long> categoryIds;

	private final Map<Long, Set<Long>> channelUsers = new ConcurrentHashMap<>();
	private final Map<Long, Long> userChannel = new ConcurrentHashMap<>();
	private final Queue<ChannelRequest> pendingRequests = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean drainingQueue = new AtomicBoolean(false);

	private final ScheduledExecutorService scheduler =
			Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "temp-channel-closer");
				t.setDaemon(true);
				return t;
			});

	private static final EnumSet<Permission> PERMISSIONS =
			EnumSet.of(Permission.VIEW_CHANNEL,
					Permission.MESSAGE_SEND,
					Permission.MESSAGE_HISTORY,
					Permission.MESSAGE_EXT_EMOJI,
					Permission.MESSAGE_ATTACH_FILES);

	@Autowired
	public DefaultTemporaryChannelService(
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

		// No channels to delete
		if (channelsToDelete.isEmpty())
			return CompletableFuture.completedFuture(null);

		log.info("[TemporaryChannelService]: Queuing {} channels for purge", channelsToDelete.size());

		// Rate-limited deletion with 1 second delay between requests
		AtomicInteger remaining = new AtomicInteger(channelsToDelete.size());
		for (int i = 0; i < channelsToDelete.size(); i++) {
			final int index = i;
			scheduler.schedule(() -> {
				channelsToDelete.get(index).delete()
						.reason("Yui restart – removing stale temp channel")
						.queue(
								__ -> {
									if (remaining.decrementAndGet() == 0) {
										log.info("TemporaryChannelService – startup purge completed");
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
						);
			}, i * 1000L, TimeUnit.MILLISECONDS);
		}

		return result;
	}

	@Override
	public CompletableFuture<TextChannel> create(Collection<Long> userIds) {
		return create(userIds, ChannelDecoration.builder()
				.name("#" + new Random().nextInt())
				.build());
	}

	@Override
	public CompletableFuture<TextChannel> create(Collection<Long> userIds, ChannelDecoration decoration) {
		if (userIds.isEmpty())
			return CompletableFuture.failedFuture(new IllegalArgumentException("No users specified"));

		Guild guild = jda.getGuilds().getFirst();
		long firstUser = userIds.iterator().next();

		Optional<TextChannel> existing = findByUser(firstUser);
		if (existing.isPresent())
			return CompletableFuture.completedFuture(existing.get());

		CompletableFuture<TextChannel> result = new CompletableFuture<>();
		tryCreateRecursive(guild, userIds, decoration, 0, result);
		return result;
	}


	/**
	 * Tries to create the channel in {@code categoryIds[index]}.
	 * On {@code CHANNEL_PARENT_MAX_CHANNELS} error automatically retries
	 * with the next category (if any).
	 */
	private void tryCreateRecursive(Guild guild, Collection<Long> userIds, ChannelDecoration decoration, int index, CompletableFuture<TextChannel> result) {
		if (index >= categoryIds.size()) {
			pendingRequests.add(new ChannelRequest(userIds, decoration, result));
			log.info("[TemporaryChannelService]: All temporary-channel categories are full – queued a request for {} user(s)",
					userIds.size());
			return;
		}

		Long cid = categoryIds.get(index);
		Category category = guild.getCategoryById(cid);
		if (category == null || category.getChannels().size() >= DISCORD_CATEGORY_LIMIT) {
			tryCreateRecursive(guild, userIds, decoration, index + 1, result);
			return;
		}

		ChannelAction<TextChannel> action = category
				.createTextChannel(decoration.getName())
				.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));

		if (decoration.getDescription() != null && !decoration.getDescription().isEmpty())
			action = action.setTopic(decoration.getDescription());

		for (long uid : userIds) {
			Member member = guild.retrieveMemberById(uid).complete();
			if (member != null)
				action = action.addPermissionOverride(member, PERMISSIONS, null);
		}

		action.queue(
				channel -> {
					channelUsers.put(channel.getIdLong(), new HashSet<>(userIds));
					userChannel.put(userIds.iterator().next(), channel.getIdLong());

					if (decoration.isMention() || (decoration.getMessage() != null && !decoration.getMessage().isBlank())) {
						StringBuilder sb = new StringBuilder();
						if (decoration.isMention())
							userIds.forEach(id -> sb.append("<@").append(id).append("> "));
						if (decoration.getMessage() != null && !decoration.getMessage().isBlank())
							sb.append(decoration.getMessage());
						channel.sendMessage(sb.toString().trim()).queue();
					}
					result.complete(channel);
				},
				failure -> {
					boolean categoryFull =
							failure instanceof ErrorResponseException ex
									&& ex.getErrorCode() == 50035
									&& ex.getMessage() != null
									&& ex.getMessage().contains("CHANNEL_PARENT_MAX_CHANNELS");

					if (categoryFull) {
						tryCreateRecursive(guild, userIds, decoration, index + 1, result);
						return;
					}

					result.completeExceptionally(failure);
				});
	}

	@Override
	public CompletableFuture<Void> close(TextChannel channel) {
		if (channel == null)
			return CompletableFuture.completedFuture(null);

		CompletableFuture<Void> future = new CompletableFuture<>();
		channel.delete()
				.reason("Closed via TemporaryChannelService")
				.queue(__ -> future.complete(null), future::completeExceptionally);
		return future;
	}

	@Override
	public CompletableFuture<Void> close(TextChannel channel, long delay) {
		if (channel == null)
			return CompletableFuture.completedFuture(null);

		CompletableFuture<Void> future = new CompletableFuture<>();

		Set<Long> userIds = channelUsers.get(channel.getIdLong());
		Long userId = (userIds != null && !userIds.isEmpty()) ? userIds.iterator().next() : null;

		EmbedBuilder builder = StyleKit.embeds().warning();
		if (userId != null) {
			builder.setTitle(Translatable.of("general.temporaryChannels.close.title", userId));
			builder.setDescription(
					Translatable.forUser("general.temporaryChannels.close.description", userId, delay));
		} else {
			builder.setTitle(Translatable.of("general.temporaryChannels.close.title"));
			builder.setDescription(Translatable.of("general.temporaryChannels.close.description", String.valueOf(delay)));
		}

		channel.sendMessageEmbeds(builder.build())
				.queue(
						_ -> scheduler.schedule(() ->
										close(channel)
												.whenComplete((_, ex) -> {
													if (ex == null)
														future.complete(null);
													else
														future.completeExceptionally(ex);
												}),
								delay, TimeUnit.SECONDS),
						future::completeExceptionally);

		return future;
	}

	@Override
	public Optional<TextChannel> findByUser(long userId) {
		Long chId = userChannel.get(userId);
		return Optional.ofNullable(chId).map(jda::getTextChannelById);
	}

	@Override
	public void reload() {
		try {
			purgeChannels().join();
		} catch (Exception ex) {
			log.warn("[TemporaryChannelService]: Purge during reload failed", ex);
		}
	}

	public void handleChannelDeletion(long channelId) {
		Set<Long> users = channelUsers.remove(channelId);
		if (users != null) users.forEach(userChannel::remove);

		drainQueue();
	}

	private void drainQueue() {
		if (!drainingQueue.compareAndSet(false, true)) return;

		scheduler.execute(() -> {
			try {
				ChannelRequest req;
				while ((req = pendingRequests.peek()) != null) {
					Guild guild = jda.getGuilds().getFirst();

					CompletableFuture<TextChannel> marker = new CompletableFuture<>();
					tryCreateRecursive(guild, req.getUserIds(), req.getDecoration(), 0, marker);

					ChannelRequest finalReq = req;
					marker.whenComplete((ch, ex) -> {
						if (ex == null) {
							pendingRequests.poll();
							finalReq.getFuture().complete(ch);
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
}