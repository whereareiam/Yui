package me.whereareiam.yui.common.conversation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.message.Translatable;
import me.whereareiam.yui.service.AuditService;
import me.whereareiam.yui.type.audit.AuditCategory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Writes a closing conversation's messages into a text file, posted as the audit entry
 * {@code conversation.transcript} (category moderation) and sent to its members, as its setup says. A transcript
 * that cannot be made or sent is logged; the conversation still closes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationTranscript {
	static final String ENTRY = "conversation.transcript";
	private static final int MESSAGES = 1000;
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final AuditService audit;
	private final JDA jda;

	CompletableFuture<Void> send(OpenConversation conversation) {
		boolean toAudit = conversation.getSetup().transcript().isAudit() && audit.isPosted(AuditCategory.MODERATION, ENTRY);
		boolean toMembers = conversation.getSetup().transcript().isMember();
		if (!toAudit && !toMembers) return CompletableFuture.completedFuture(null);

		return conversation.getChannel().getIterableHistory().takeAsync(MESSAGES)
				.thenCompose(messages -> {
					byte[] text = text(conversation, messages);
					CompletableFuture<Void> audited = toAudit
							? audit.audit(AuditCategory.MODERATION, ENTRY, message(conversation, text))
							: CompletableFuture.completedFuture(null);

					return toMembers ? audited.thenCompose(ignored -> members(conversation, text)) : audited;
				})
				.exceptionally(error -> {
					log.warn("[Conversations] No transcript of the {} conversation of {}", conversation.getPurpose(), conversation.getMembers(), error);
					return null;
				});
	}

	private CompletableFuture<Void> members(OpenConversation conversation, byte[] text) {
		return CompletableFuture.allOf(conversation.getMembers().stream()
				.map(member -> jda.retrieveUserById(member).submit()
						.thenCompose(user -> user.openPrivateChannel().submit())
						.thenCompose(channel -> channel.sendMessage(message(conversation, text)).submit())
						.exceptionally(error -> null))
				.toArray(CompletableFuture[]::new));
	}

	private static MessageCreateData message(OpenConversation conversation, byte[] text) {
		return new MessageCreateBuilder()
				.setContent(Translatable.text("conversations.transcript").with("purpose", conversation.getPurpose()).resolveDefault()
						+ " " + String.join(" ", conversation.getMembers().stream().map(member -> "<@" + member + ">").toList()))
				.addFiles(FileUpload.fromData(text, conversation.getPurpose() + "-transcript.txt"))
				.build();
	}

	private static byte[] text(OpenConversation conversation, List<Message> newestFirst) {
		StringBuilder text = new StringBuilder();
		for (Message message : newestFirst.reversed())
			text.append('[').append(TIME.format(message.getTimeCreated())).append("] ")
					.append(message.getAuthor().getName()).append(": ")
					.append(message.getContentDisplay()).append('\n');

		return text.toString().getBytes(StandardCharsets.UTF_8);
	}
}
