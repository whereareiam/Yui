package me.whereareiam.yui.common.listener.guild;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.fluctlight.FluctlightService;
import me.whereareiam.yui.common.role.rule.LanguageRoleRule;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * Gives a member who joins the roles of the languages they speak, so a member who rejoins gets them back.
 */
@Component
@RequiredArgsConstructor
public class GuildMemberJoinListener extends ListenerAdapter {
	private final FluctlightService fluctlightService;
	private final LanguageRoleRule languageRoleRule;

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		fluctlightService.get(event.getUser().getIdLong()).ifPresent(languageRoleRule::grant);
	}
}
