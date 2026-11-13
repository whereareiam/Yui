package me.whereareiam.yui.adapter.command.executor;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.annotation.command.Optional;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.message.Translatable;
import me.whereareiam.yui.model.config.audit.AuditTrail;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.persistence.AuditPersistence;
import me.whereareiam.yui.util.style.StyleKit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * {@code /audit [entry]}: the newest kept entries of the audit trail, those whose name contains {@code entry} if
 * given. Needs {@code audit.yml}'s {@code store}.
 */
@Component
@RequiredArgsConstructor
public class AuditCommand {
	private static final int SHOWN = 10;
	private static final int DESCRIPTION = 4000;

	private final ObjectProvider<AuditTrail> trail;
	private final ObjectProvider<AuditPersistence> persistence;

	@Definition("audit")
	@Command("audit [entry]")
	public void onCommand(Interaction interaction, @Optional @Argument("entry") String entry) {
		Fluctlight fluctlight = interaction.fluctlight();
		EmbedBuilder embed = StyleKit.embeds().info().setTitle(Translatable.text("commands.audit.title").resolve(fluctlight));

		AuditPersistence kept = persistence.getIfAvailable();
		if (!trail.getObject().getStore().isEnabled() || kept == null) {
			reply(interaction, embed.setDescription(Translatable.text("commands.audit.disabled").resolve(fluctlight)));
			return;
		}

		String filter = entry == null || entry.isBlank() ? null : entry.trim().toLowerCase(Locale.ROOT);
		List<AuditPersistence.Record> records = kept.search(null, filter, SHOWN);
		reply(interaction, embed.setDescription(records.isEmpty()
				? Translatable.text("commands.audit.empty").resolve(fluctlight)
				: lines(records, fluctlight)));
	}

	private static String lines(List<AuditPersistence.Record> records, Fluctlight fluctlight) {
		StringBuilder text = new StringBuilder();
		for (AuditPersistence.Record record : records) {
			String line = Translatable.text("commands.audit.line")
					.with("posted", TimeFormat.RELATIVE.format(record.postedAt()))
					.with("category", record.category().name().toLowerCase(Locale.ROOT))
					.with("entry", record.entry())
					.with("content", record.content())
					.resolve(fluctlight);
			if (text.length() + line.length() + 2 > DESCRIPTION) break;

			text.append(line).append("\n\n");
		}

		return text.toString().trim();
	}

	private static void reply(Interaction interaction, EmbedBuilder embed) {
		interaction.replyCallback().replyEmbeds(embed.build()).setEphemeral(true).queue();
	}
}
