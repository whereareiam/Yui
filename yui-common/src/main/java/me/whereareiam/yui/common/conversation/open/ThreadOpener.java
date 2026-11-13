package me.whereareiam.yui.common.conversation.open;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.common.conversation.ConversationSetup;
import me.whereareiam.yui.common.conversation.OpenConversation;
import me.whereareiam.yui.model.config.conversation.Conversations;
import me.whereareiam.yui.type.ConversationType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Opens a private thread under the setup's parent channel, adds the members and the staff's members, and greets
 * them. Discord archives it after {@code types.thread.archive} without messages; closing archives and locks it, so
 * it stays readable for staff.
 */
@Component
@RequiredArgsConstructor
public class ThreadOpener implements ConversationOpener {
	private final JDA jda;
	private final ObjectProvider<Conversations> config;

	@Override
	public ConversationType type() {
		return ConversationType.THREAD;
	}

	@Override
	public CompletableFuture<OpenConversation> open(String purpose, Set<Long> members, ConversationSetup setup) {
		TextChannel parent = setup.parent() == null ? null : jda.getTextChannelById(setup.parent());
		if (parent == null)
			return CompletableFuture.failedFuture(new IllegalStateException("No text channel " + setup.parent() + " to make threads under"));

		Conversations.Thread thread = config.getObject().getTypes().getThread();
		String name = Objects.requireNonNullElse(setup.name(), purpose);

		return parent.createThreadChannel(name, true)
				.setInvitable(thread.isInvitable())
				.setAutoArchiveDuration(archive(thread))
				.submit()
				.thenCompose(channel -> join(channel, members, setup).thenApply(ignored -> conversation(purpose, members, setup, channel)));
	}

	private CompletableFuture<?> join(ThreadChannel channel, Set<Long> members, ConversationSetup setup) {
		Set<Long> people = new LinkedHashSet<>(members);
		for (String staff : setup.staff()) {
			Role role = channel.getGuild().getRoleById(staff);
			if (role != null) channel.getGuild().getMembersWithRoles(role).stream().map(Member::getIdLong).forEach(people::add);
		}

		CompletableFuture<?> joined = CompletableFuture.allOf(people.stream()
				.map(person -> channel.addThreadMemberById(person).submit())
				.toArray(CompletableFuture[]::new));
		String greeting = ConversationOpener.greeting(members, setup);

		return greeting.isEmpty() ? joined : joined.thenCompose(ignored -> channel.sendMessage(greeting).submit());
	}

	private static ThreadChannel.AutoArchiveDuration archive(Conversations.Thread thread) {
		int minutes = (int) (thread.getArchive().getSeconds() / 60);

		return ThreadChannel.AutoArchiveDuration.fromKey(minutes);
	}

	private static OpenConversation conversation(String purpose, Set<Long> members, ConversationSetup setup, ThreadChannel channel) {
		return new OpenConversation(purpose, members, ConversationType.THREAD, channel, setup,
				() -> channel.getManager().setArchived(true).setLocked(true).submit());
	}
}
