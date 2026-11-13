package me.whereareiam.yui.common.audit.type.user;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.Constants;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import me.whereareiam.yui.api.util.Audit;
import me.whereareiam.yui.api.util.style.StyleKit;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class UserKickAudit extends ListenerAdapter {
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		event.getGuild()
				.retrieveAuditLogs()
				.type(ActionType.KICK)
				.limit(1)
				.queue(entries -> {
					if (entries.isEmpty()) return;

					AuditLogEntry entry = entries.getFirst();
					if (entry.getTargetIdLong() != event.getUser().getIdLong()) return;

					OffsetDateTime kickTime = entry.getTimeCreated();
					if (kickTime.isBefore(OffsetDateTime.now().minusSeconds(5))) return;

					User target = event.getUser();
					User moderator = entry.getUser();

					final String reason = entry.getReason() != null && !entry.getReason().isBlank()
							? entry.getReason()
							: "No reason provided";

					Audit.log(AuditCategory.MODERATION, Constants.AuditEntries.MEMBER_KICK)
							.withLocalizedEmbed(locale -> {
								String title = Translatable.text("audit.member.kick.title").resolve(locale);
								String description = Translatable.text("audit.member.kick.description")
										.with("mention", target.getAsMention())
										.resolve(locale);
								String targetField = Translatable.text("audit.member.kick.fields.target").resolve(locale);
								String reasonField = Translatable.text("audit.member.kick.fields.reason").resolve(locale);
								String moderatorField = Translatable.text("audit.member.kick.fields.moderator").resolve(locale);

								return StyleKit.embeds().error()
										.setTitle(title)
										.setDescription(description)
										.addField(targetField, target.getAsMention(), true)
										.addField(moderatorField, moderator != null
												? moderator.getAsMention()
												: "Unknown", true
										).addField(reasonField, reason, false)
										.setThumbnail(target.getEffectiveAvatarUrl())
										.setTimestamp(Instant.now())
										.build();
							})
							.send();
				});
	}
}
