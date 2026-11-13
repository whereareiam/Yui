package me.whereareiam.yui.verification.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import me.whereareiam.yui.api.util.Audit;
import me.whereareiam.yui.api.util.style.StyleKit;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Posts verification's audit entries ({@code verification.started} and the others) in the members category, with
 * the texts of {@code messages/<locale>/audit.yml}.
 */
@Component
public class VerificationAudit {
	/**
	 * Verification's audit entries, each with the embed kind it is posted in.
	 */
	@Getter
	@RequiredArgsConstructor
	public enum Entry {
		STARTED("started", () -> StyleKit.embeds().info()),
		COMPLETED("completed", () -> StyleKit.embeds().success()),
		FAILED("failed", () -> StyleKit.embeds().error()),
		ABANDONED("abandoned", () -> StyleKit.embeds().warning()),
		TIMEOUT("timeout", () -> StyleKit.embeds().warning());

		private final String name;
		private final Supplier<EmbedBuilder> kind;

		/**
		 * @return the entry's name in {@code audit.yml}, such as {@code verification.started}
		 */
		public String entry() {
			return "verification." + name;
		}
	}

	public void post(Entry entry, long member) {
		post(entry, member, null);
	}

	public void post(Entry entry, long member, @Nullable Long initiator) {
		String texts = "verification.audit." + entry.getName() + ".";
		Audit.log(AuditCategory.MEMBERS, entry.entry())
				.withLocalizedEmbed(locale -> entry.getKind().get()
						.setTitle(Translatable.text(texts + "title").resolve(locale))
						.setDescription(Translatable.text(texts + "description")
								.with("mention", "<@" + member + ">")
								.with("initiator", initiator == null ? "none" : "<@" + initiator + ">")
								.resolve(locale))
						.build())
				.send();
	}
}
