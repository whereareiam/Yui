package me.whereareiam.yui.adapter.command.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.command.YuiCommandManager;
import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.adapter.command.mapper.FluctlightInteractionMapper;
import me.whereareiam.yui.adapter.command.parser.FluctlightParser;
import me.whereareiam.yui.adapter.command.requirements.CommandRequirementsPostprocessor;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.JDA;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.slash.DiscordSetting;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.parser.ParserDescriptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;

/**
 * FactoryBean for creating and configuring the JDA6CommandManager.
 * <p>
 * This ensures proper lifecycle order: JDA must be ready before
 * the command manager is created, and the listener is wired into JDA
 * during bean creation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommandManagerFactory implements FactoryBean<JDA6CommandManager<Interaction>> {
	private final CommandRequirementsPostprocessor requirementsPostprocessor;
	private final FluctlightInteractionMapper fluctlightInteractionMapper;
	private final ScheduledExecutorService scheduledExecutorService;
	private final CommandDefinitionRegistry definitionRegistry;
	private final FluctlightParser fluctlightParser;
	private final JDA jda;

	private JDA6CommandManager<Interaction> commandManager;

	@Override
	public JDA6CommandManager<Interaction> getObject() {
		if (commandManager == null) {
			log.debug("Creating YuiCommandManager with async execution coordinator");

			commandManager = new YuiCommandManager(
					ExecutionCoordinator.asyncCoordinator(),
					fluctlightInteractionMapper,
					scheduledExecutorService,
					definitionRegistry,
					fluctlightParser,
					jda
			);

			// Configure Discord settings
			commandManager.discordSettings().set(DiscordSetting.AUTO_REGISTER_SLASH_COMMANDS, true);
			commandManager.discordSettings().set(DiscordSetting.EPHEMERAL_ERROR_MESSAGES, true);

			// Register Fluctlight parser for user arguments
			commandManager.parserRegistry().registerParser(ParserDescriptor.of(fluctlightParser, Fluctlight.class));

			// Register global command postprocessors (runs after parsing, can access command meta)
			commandManager.registerCommandPostProcessor(requirementsPostprocessor);

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
}
