package me.whereareiam.yui.common.audit.type;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.util.Audit;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
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
					String title = Translatable.text("messages.audit.user.leave.title").resolveDefault();
					String description = Translatable.text("messages.audit.user.leave.description")
							.with("mention", event.getUser().getAsMention())
							.resolveDefault();
					String targetField = Translatable.text("messages.audit.user.leave.fields.target").resolveDefault();

					Audit.log(Constants.AuditTypes.USER_LEAVE)
							.withEmbed(StyleKit.embeds().info()
									.setTitle(title)
									.setDescription(description)
									.addField(targetField, event.getUser().getAsMention(), true)
									.setThumbnail(event.getUser().getEffectiveAvatarUrl())
									.setTimestamp(Instant.now())
									.build())
							.send();
				});
	}
}
