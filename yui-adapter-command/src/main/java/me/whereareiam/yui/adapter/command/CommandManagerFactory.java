package me.whereareiam.yui.adapter.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.discord.slash.DiscordSetting;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * FactoryBean for creating and configuring the JDA6CommandManager.
 * <p>
 * This ensures proper initialization order: JDA must be ready before
 * the command manager is created, and the listener is wired into JDA
 * during bean creation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommandManagerFactory implements FactoryBean<JDA6CommandManager<JDAInteraction>> {
	private final JDA jda;
	private JDA6CommandManager<JDAInteraction> commandManager;

	@Override
	public JDA6CommandManager<JDAInteraction> getObject() {
		if (commandManager == null) {
			log.debug("Creating JDA6CommandManager with async execution coordinator");
			
			commandManager = new JDA6CommandManager<>(
					ExecutionCoordinator.asyncCoordinator(),
					JDAInteraction.InteractionMapper.identity()
			);

			// Configure Discord settings
			commandManager.discordSettings().set(DiscordSetting.AUTO_REGISTER_SLASH_COMMANDS, true);
			commandManager.discordSettings().set(DiscordSetting.EPHEMERAL_ERROR_MESSAGES, true);

			// Wire the command listener into JDA
			log.debug("Registering command listener with JDA");
			jda.addEventListener(commandManager.createListener());
		}
		return commandManager;
	}

	@Override
	public Class<?> getObjectType() {
		return JDA6CommandManager.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}