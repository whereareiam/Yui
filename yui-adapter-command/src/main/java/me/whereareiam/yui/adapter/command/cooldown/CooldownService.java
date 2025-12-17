package me.whereareiam.yui.adapter.command.cooldown;

import me.whereareiam.yui.model.command.CommandCooldown;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class CooldownService {
	private final Map<String, Map<String, Long>> cooldownMap = new HashMap<>();

	public boolean isOnCooldown(String userId, CommandCooldown cc) {
		if (cc == null || !cc.isEnabled())
			return false;

		Map<String, Long> userCooldowns = cooldownMap.getOrDefault(userId, new HashMap<>());
		Long nextAllowed = userCooldowns.get(cc.getGroup());
		if (nextAllowed == null)
			return false;

		return Instant.now().getEpochSecond() < nextAllowed;
	}

	public void markUsage(String userId, CommandCooldown cc) {
		if (cc == null || !cc.isEnabled())
			return;

		Map<String, Long> userCooldowns = cooldownMap.computeIfAbsent(userId, _ -> new HashMap<>());
		long now = Instant.now().getEpochSecond();
		userCooldowns.put(cc.getGroup(), now + cc.getCooldown());
	}

	public boolean handleCooldown(SlashCommandInteractionEvent event, CommandCooldown cooldown) {
		String userId = event.getUser().getId();

		if (isOnCooldown(userId, cooldown)) {
			event.reply("You are on cooldown for this command.").setEphemeral(true).queue();
			return true;
		}

		markUsage(userId, cooldown);
		return false;
	}
}