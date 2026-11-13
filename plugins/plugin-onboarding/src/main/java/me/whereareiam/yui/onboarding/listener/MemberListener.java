package me.whereareiam.yui.onboarding.listener;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.onboarding.model.OnboardingSettings;
import me.whereareiam.yui.onboarding.service.DefaultOnboardingService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * A joining member verifies, when {@code member-join} says so, or gets the role back when verified already; a
 * leaving member's onboarding ends.
 */
@Component
@RequiredArgsConstructor
public class MemberListener extends ListenerAdapter {
	private final DefaultOnboardingService onboarding;
	private final ObjectProvider<OnboardingSettings> settings;

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		long member = event.getUser().getIdLong();
		boolean join = settings.getObject().getTriggers().isMemberJoin();
		if (join || onboarding.isVerified(member)) onboarding.start(member, null);
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		onboarding.left(event.getUser().getIdLong());
	}
}
