package me.whereareiam.yui.common.audit.type.user;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.Constants;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import me.whereareiam.yui.api.util.Audit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class UserLeaveAudit extends ListenerAdapter {
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		// Check if this was a kick by querying audit logs
		event.getGuild().retrieveAuditLogs()
				.type(ActionType.KICK)
				.limit(1)
				.queue(entries -> {
					// Check if this was a kick
					if (!entries.isEmpty()) {
						AuditLogEntry entry = entries.getFirst();
						
						// If the kick was for this user and happened recently, skip logging as leave
						if (entry.getTargetIdLong() == event.getUser().getIdLong() &&
							entry.getTimeCreated().isAfter(OffsetDateTime.now().minusSeconds(5))) {
							return; // This is a kick, UserKickAudit will handle it
						}
					}

					// This is a normal leave
					Audit.log(AuditCategory.MEMBERS, Constants.AuditEntries.MEMBER_LEAVE)
							.withLocalizedEmbed(locale -> {
								String title = Translatable.text("audit.member.leave.title").resolve(locale);
								String description = Translatable.text("audit.member.leave.description")
										.with("mention", event.getUser().getAsMention())
										.resolve(locale);
								String targetField = Translatable.text("audit.member.leave.fields.target").resolve(locale);

								return new EmbedBuilder()
										.setTitle(title)
										.setDescription(description)
										.addField(targetField, event.getUser().getAsMention(), true)
										.setThumbnail(event.getUser().getEffectiveAvatarUrl())
										.setTimestamp(Instant.now())
										.build();
							})
							.send();
				});
	}
}
