package me.whereareiam.yue.adapter.command.scanner;

import me.whereareiam.yue.adapter.command.registry.CommandDefinition;
import me.whereareiam.yue.adapter.command.registry.CommandRegistry;
import me.whereareiam.yue.api.output.command.Command;
import me.whereareiam.yue.api.output.command.CommandBase;
import me.whereareiam.yue.api.output.service.CommandService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class CommandScanner implements ApplicationContextAware {
	private static final Logger logger = LoggerFactory.getLogger(CommandScanner.class);

	private final CommandRegistry registry;
	private final CommandService commandService;

	private ApplicationContext ctx;

	@Autowired
	public CommandScanner(CommandRegistry registry,
	                      CommandService commandService) {
		this.registry = registry;
		this.commandService = commandService;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.ctx = applicationContext;
	}

	public void scanAnnotatedCommands() {
		Map<String, CommandBase> commandBeans = ctx.getBeansOfType(CommandBase.class);

		for (CommandBase bean : commandBeans.values()) {
			Class<?> clazz = bean.getClass();

			boolean hasClassCommand = clazz.isAnnotationPresent(Command.class);

			// Register class-level command
			if (hasClassCommand) {
				Command ann = clazz.getAnnotation(Command.class);
				registerSlashCommand(bean, clazz, ann.name());
			}

			// Register method-level commands only if no class-level command
			if (!hasClassCommand) {
				for (Method method : clazz.getDeclaredMethods()) {
					if (method.isAnnotationPresent(Command.class)) {
						Command ann = method.getAnnotation(Command.class);
						registerSlashCommand(bean, method, ann.name(), clazz);
					}
				}
			}
		}
	}

	private void registerSlashCommand(Object bean, Class<?> clazz, String commandName) {
		me.whereareiam.yue.api.model.command.Command command = commandService.getCommand(commandName);
		if (isCommandEnabledForRegistration(command, commandName)) {
			logger.debug("Skipped class-level command from bean: {}", bean.getClass().getName());
			return;
		}

		try {
			Method method = clazz.getMethod("onCommand", SlashCommandInteractionEvent.class);
			CommandDefinition def = new CommandDefinition(commandName, command, bean, method);
			registry.register(def);
			logger.info("Registered slash command '{}' from {} (class-level).", commandName, clazz.getSimpleName());
		} catch (NoSuchMethodException e) {
			logger.warn("Class-level command '{}' found but no 'onCommand(SlashCommandInteractionEvent)' method. Skipping.", commandName);
		}
	}

	private void registerSlashCommand(Object bean, Method method, String commandName, Class<?> declaringClass) {
		me.whereareiam.yue.api.model.command.Command command = commandService.getCommand(commandName);
		if (isCommandEnabledForRegistration(command, commandName)) {
			logger.debug("Skipped method-level command: {}.{}", bean.getClass().getName(), method.getName());
			return;
		}

		CommandDefinition def = new CommandDefinition(commandName, command, bean, method);
		registry.register(def);

		logger.info("Registered slash command '{}' from {}#{} (method-level).",
				commandName, declaringClass.getSimpleName(), method.getName());
	}

	private boolean isCommandEnabledForRegistration(me.whereareiam.yue.api.model.command.Command command, String commandName) {
		if (command == null) {
			logger.warn("No config found for command '{}'; skipping.", commandName);
			return true;
		}

		if (!command.isEnabled()) {
			logger.info("Command '{}' is disabled in config; skipping registration.", commandName);
			return true;
		}

		return false;
	}
}
