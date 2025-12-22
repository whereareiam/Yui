package me.whereareiam.yui.common.audit.type;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.util.Audit;
import me.whereareiam.yui.util.translation.Translatable;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserJoinAudit extends ListenerAdapter {
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		String title = Translatable.text("messages.audit.user.join.title").resolveDefault();
		String description = Translatable.text("messages.audit.user.join.description")
				.with("mention", event.getUser().getAsMention())
				.resolveDefault();
		String targetField = Translatable.text("messages.audit.user.join.fields.target").resolveDefault();
		String accountCreatedField = Translatable.text("messages.audit.user.join.fields.accountCreated").resolveDefault();

		Audit.log(Constants.AuditTypes.USER_JOIN)
				.withEmbed(embed -> embed
						.setTitle(title)
						.setDescription(description)
						.addField(targetField, event.getUser().getAsMention(), true)
						.addField(accountCreatedField, "<t:" + event.getUser().getTimeCreated().toEpochSecond() + ":R>", true)
						.setThumbnail(event.getUser().getEffectiveAvatarUrl())
						.setTimestamp(Instant.now()))
				.send();
	}
}
