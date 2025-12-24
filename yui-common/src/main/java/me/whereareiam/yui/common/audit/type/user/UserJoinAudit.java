package me.whereareiam.yui.common.audit.type.user;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.util.Audit;
import me.whereareiam.yui.util.translation.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserJoinAudit extends ListenerAdapter {
	@Override
	public void onGuildMemberJoin(@NonNull GuildMemberJoinEvent event) {
		Audit.log(Constants.AuditTypes.USER_JOIN)
				.withLocalizedEmbed(locale -> {
					String title = Translatable.text("audit.user.join.title").resolve(locale);
					String description = Translatable.text("audit.user.join.description")
							.with("mention", event.getUser().getAsMention())
							.resolve(locale);
					String targetField = Translatable.text("audit.user.join.fields.target").resolve(locale);
					String accountCreatedField = Translatable.text("audit.user.join.fields.accountCreated").resolve(locale);
					return new EmbedBuilder()
							.setTitle(title)
							.setDescription(description)
							.addField(targetField, event.getUser().getAsMention(), true)
							.addField(accountCreatedField, "<t:" + event.getUser().getTimeCreated().toEpochSecond() + ":R>", true)
							.setThumbnail(event.getUser().getEffectiveAvatarUrl())
							.setTimestamp(Instant.now())
							.build();
				})
				.send();
	}
}
