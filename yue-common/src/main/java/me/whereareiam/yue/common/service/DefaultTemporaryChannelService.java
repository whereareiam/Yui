package me.whereareiam.yue.common.service;

import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yue.api.input.TemporaryChannelService;
import me.whereareiam.yue.api.model.ChannelDecoration;
import me.whereareiam.yue.api.model.config.settings.Settings;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DefaultTemporaryChannelService implements TemporaryChannelService {
	private final JDA jda;
	private final long categoryId;

	private final Map<Long, Set<Long>> channelUsers = new ConcurrentHashMap<>();
	private final Map<Long, Long> userChannel = new ConcurrentHashMap<>();

	private static final EnumSet<Permission> PERMISSIONS =
			EnumSet.of(Permission.VIEW_CHANNEL,
					Permission.MESSAGE_SEND,
					Permission.MESSAGE_HISTORY,
					Permission.MESSAGE_EXT_EMOJI,
					Permission.MESSAGE_ATTACH_FILES);

	@Autowired
	public DefaultTemporaryChannelService(
			JDA jda,
			Settings settings
	) {
		this.jda = jda;
		this.categoryId = Long.parseLong(settings.getDiscord().getChannels().getTempChannelCategoryId());
	}

	public void purgeChannels() {
		jda.getGuilds().forEach(g -> {
			Category category = g.getCategoryById(categoryId);
			if (category == null) return;

			category.getChannels().forEach(ch ->
					ch.delete().reason("Yue restart – removing stale temp channel").queue());
		});
		log.info("TemporaryChannelService – startup purge completed");
	}

	@Override
	public CompletableFuture<TextChannel> create(Collection<Long> userIds) {
		return create(userIds, ChannelDecoration.builder()
				.name("#" + new Random().nextInt())
				.build()
		);
	}

	@Override
	public CompletableFuture<TextChannel> create(Collection<Long> userIds, ChannelDecoration decoration) {
		if (userIds.isEmpty())
			return CompletableFuture.failedFuture(new IllegalArgumentException("No users specified"));

		Guild guild = jda.getGuilds().getFirst();
		Category category = guild.getCategoryById(categoryId);
		if (category == null)
			return CompletableFuture.failedFuture(new IllegalArgumentException("Category doesn't exist"));

		long firstUser = userIds.iterator().next();
		Optional<TextChannel> existing = findByUser(firstUser);
		if (existing.isPresent())
			return CompletableFuture.completedFuture(existing.get());

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

		CompletableFuture<TextChannel> future = new CompletableFuture<>();
		action.queue(channel -> {
			channelUsers.put(channel.getIdLong(), new HashSet<>(userIds));
			userChannel.put(firstUser, channel.getIdLong());

			if (decoration.isMention() || (decoration.getMessage() != null && !decoration.getMessage().isBlank())) {
				StringBuilder sb = new StringBuilder();

				if (decoration.isMention())
					userIds.forEach(id -> sb.append("<@").append(id).append("> "));

				if (decoration.getMessage() != null && !decoration.getMessage().isBlank())
					sb.append(decoration.getMessage());

				channel.sendMessage(sb.toString().trim()).queue();
			}

			future.complete(channel);
		}, future::completeExceptionally);

		return future;
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
	public Optional<TextChannel> findByUser(long userId) {
		Long chId = userChannel.get(userId);

		return Optional.ofNullable(chId).map(jda::getTextChannelById);
	}

	public void handleChannelDeletion(long channelId) {
		Set<Long> users = channelUsers.remove(channelId);
		if (users != null) {
			users.forEach(userChannel::remove);
		}
	}
}
