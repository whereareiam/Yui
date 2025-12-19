package me.whereareiam.yui.adapter.command.registration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.command.YuiCommandManager;
import net.dv8tion.jda.api.JDA;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.discord.slash.DiscordSetting;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Command registration handler that integrates Cloud's root command deletion
 * with JDA slash command registration for Yui.
 * <p>
 * On registration, if JDA is ready and auto-registration is enabled, we
 * synchronize Discord slash commands with the current command tree.
 * This ensures commands appear in Discord even when they're registered
 * after JDA has already fired its Ready events.
 * <p>
 * On unregistration we simply re-synchronize the slash commands with the
 * current command tree after Cloud has marked a root command for deletion.
 */
@Slf4j
@RequiredArgsConstructor
public final class JDARegistrationHandler implements CommandRegistrationHandler<JDAInteraction> {
	private final ScheduledExecutorService scheduledPool;
	private final YuiCommandManager commandManager;
	private final JDA jda;

	private final AtomicBoolean syncScheduled = new AtomicBoolean(false);

	@Override
	public boolean registerCommand(@NotNull Command<JDAInteraction> command) {
		if (!commandManager.discordSettings().get(DiscordSetting.AUTO_REGISTER_SLASH_COMMANDS) || jda.getStatus() != JDA.Status.CONNECTED)
			return true;
		
		if (!syncScheduled.compareAndSet(false, true))
			return true;
		
		// Schedule sync after a short delay to batch multiple command registrations
		scheduledPool.schedule(() -> {
			try {
				syncScheduled.set(false);
				log.debug("Synchronizing Discord slash commands");
				
				// Register global commands
				commandManager.registerGlobalCommands(jda);
				
				// Register guild-specific commands for all known guilds
				jda.getGuilds().forEach(commandManager::registerGuildCommands);
			} catch (Exception e) {
				syncScheduled.set(false);
				log.warn("Failed to synchronize Discord commands after command registration", e);
			}
		}, 100, TimeUnit.MILLISECONDS);
		
		return true;
	}

	@Override
	public void unregisterRootCommand(@NotNull CommandComponent<JDAInteraction> rootCommand) {
		log.debug("Re-synchronizing Discord slash commands after root command '{}' deletion", rootCommand.name());

		try {
			// Global commands
			commandManager.registerGlobalCommands(jda);

			// Guild-specific commands for all known guilds
			jda.getGuilds().forEach(commandManager::registerGuildCommands);
		} catch (Exception e) {
			log.warn("Failed to re-register Discord commands after root command deletion", e);
		}
	}
}

