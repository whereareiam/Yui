package me.whereareiam.yui.adapter.command.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.discord.jda6.JDACommandFactory;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Command registration handler that integrates Cloud's root command deletion
 * with JDA slash command registration for Yui.
 * <p>
 * On registration we currently don't need to perform any extra work because
 * Cloud-JDA's {@link JDACommandFactory} derives
 * slash commands from the current command tree when
 * {@link YuiCommandManager#registerGlobalCommands(JDA)}
 * or {@link YuiCommandManager#registerGuildCommands(Guild)}
 * are invoked.
 * <p>
 * On unregistration we simply re-synchronize the slash commands with the
 * current command tree after Cloud has marked a root command for deletion.
 */
@Slf4j
@RequiredArgsConstructor
final class JDARegistrationHandler implements CommandRegistrationHandler<JDAInteraction> {
	private final YuiCommandManager commandManager;
	private final JDA jda;

	@Override
	public boolean registerCommand(@NotNull Command<JDAInteraction> command) {
		// JDA6CommandManager is responsible for generating slash commands from the
		// current command tree. We don't need to perform any native registration
		// work here, so we simply report success.
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

