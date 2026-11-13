package me.whereareiam.yui.common.audit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.StringJoiner;

/**
 * An audit message as the plain text the store keeps: its content, then each embed's title, description and fields.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class AuditText {
	static String of(MessageCreateData message) {
		StringJoiner text = new StringJoiner("\n");
		if (!message.getContent().isBlank()) text.add(message.getContent());

		for (MessageEmbed embed : message.getEmbeds()) {
			if (embed.getTitle() != null) text.add(embed.getTitle());
			if (embed.getDescription() != null) text.add(embed.getDescription());
			embed.getFields().forEach(field -> text.add(field.getName() + ": " + field.getValue()));
		}

		return text.toString();
	}
}
