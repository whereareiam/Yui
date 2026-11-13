package me.whereareiam.yui.common.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.semantica.message.ResolvedMessage;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

/**
 * Turns a resolved message into text for Discord: what translators wrote is kept with its markdown, and every value
 * the code inserted is escaped, so a member named {@code **Bob**} is shown as written.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class DiscordRenderer {
	static String render(ResolvedMessage message) {
		StringBuilder text = new StringBuilder();
		for (ResolvedMessage.Part part : message.getParts()) {
			switch (part) {
				case ResolvedMessage.Literal literal -> text.append(literal.text());
				case ResolvedMessage.Value value -> text.append(MarkdownSanitizer.escape(value.text()));
			}
		}

		return text.toString();
	}
}
