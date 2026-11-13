package me.whereareiam.yui.common.conversation.open;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.model.config.conversation.Conversations;
import me.whereareiam.yui.api.type.ConversationType;
import me.whereareiam.yui.common.conversation.ConversationSetup;
import me.whereareiam.yui.common.conversation.OpenConversation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Opens a temporary text channel in the first configured category with room (Discord allows 50 channels each),
 * visible to the members and the staff roles only. Closing deletes it.
 */
@Component
@RequiredArgsConstructor
public class ChannelOpener implements ConversationOpener {
	private static final int CATEGORY_LIMIT = 50;
	private static final EnumSet<Permission> SPEAK = EnumSet.of(
			Permission.VIEW_CHANNEL,
			Permission.MESSAGE_SEND,
			Permission.MESSAGE_HISTORY,
			Permission.MESSAGE_EXT_EMOJI,
			Permission.MESSAGE_ATTACH_FILES
	);

	private final JDA jda;
	private final ObjectProvider<Conversations> config;

	@Override
	public ConversationType type() {
		return ConversationType.CHANNEL;
	}

	@Override
	public CompletableFuture<OpenConversation> open(String purpose, Set<Long> members, ConversationSetup setup) {
		Optional<Category> category = categories().stream()
				.map(jda::getCategoryById)
				.filter(Objects::nonNull)
				.filter(candidate -> candidate.getChannels().size() < CATEGORY_LIMIT)
				.findFirst();
		if (category.isEmpty())
			return CompletableFuture.failedFuture(new IllegalStateException("Every conversation category is full or missing"));

		return create(category.get(), purpose, members, setup)
				.thenCompose(channel -> greet(channel, members, setup).thenApply(ignored -> conversation(purpose, members, setup, channel)));
	}

	/**
	 * Deletes what earlier runs left in the categories, when {@code types.channel.purge} says so.
	 *
	 * @return completes once every leftover is deleted
	 */
	public CompletableFuture<Void> purge() {
		Conversations.Channel channel = config.getObject().getTypes().getChannel();
		if (!channel.isEnabled() || !channel.isPurge()) return CompletableFuture.completedFuture(null);

		return CompletableFuture.allOf(categories().stream()
				.map(jda::getCategoryById)
				.filter(Objects::nonNull)
				.flatMap(category -> category.getChannels().stream())
				.map(leftover -> leftover.delete().reason("Left from an earlier run of Yui").submit())
				.toArray(CompletableFuture[]::new));
	}

	private List<String> categories() {
		return config.getObject().getTypes().getChannel().getCategories();
	}

	private static CompletableFuture<TextChannel> create(Category category, String purpose, Set<Long> members, ConversationSetup setup) {
		Guild guild = category.getGuild();
		ChannelAction<TextChannel> action = category.createTextChannel(Objects.requireNonNullElse(setup.name(), purpose))
				.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));
		for (long member : members)
			action = action.addMemberPermissionOverride(member, SPEAK, null);

		for (String staff : setup.staff()) {
			Role role = guild.getRoleById(staff);
			if (role != null) action = action.addPermissionOverride(role, SPEAK, null);
		}

		return action.submit();
	}

	private static CompletableFuture<?> greet(TextChannel channel, Set<Long> members, ConversationSetup setup) {
		String greeting = ConversationOpener.greeting(members, setup);

		return greeting.isEmpty() ? CompletableFuture.completedFuture(null) : channel.sendMessage(greeting).submit();
	}

	private static OpenConversation conversation(String purpose, Set<Long> members, ConversationSetup setup, TextChannel channel) {
		return new OpenConversation(purpose, members, ConversationType.CHANNEL, channel, setup,
				() -> channel.delete().reason("The conversation closed").submit());
	}
}
