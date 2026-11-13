package me.whereareiam.yui.verification.listener;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.verification.model.VerificationSettings;
import me.whereareiam.yui.verification.service.DefaultVerificationService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * A joining member verifies, when {@code member-join} says so, or gets the role back when verified already; a
 * leaving member's verification ends.
 */
@Component
@RequiredArgsConstructor
public class MemberListener extends ListenerAdapter {
	private final DefaultVerificationService verification;
	private final ObjectProvider<VerificationSettings> settings;

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		long member = event.getUser().getIdLong();
		boolean join = settings.getObject().getTriggers().isMemberJoin();
		if (join || verification.isVerified(member)) verification.start(member, null);
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		verification.left(event.getUser().getIdLong());
	}
}
