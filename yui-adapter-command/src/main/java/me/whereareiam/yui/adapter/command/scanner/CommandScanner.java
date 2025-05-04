package me.whereareiam.yui.adapter.command.scanner;

import me.whereareiam.yui.adapter.command.registry.CommandDefinition;
import me.whereareiam.yui.adapter.command.registry.CommandRegistry;
import me.whereareiam.yui.api.annotation.Command;
import me.whereareiam.yui.api.output.CommandBase;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Scans for beans of type CommandBase and links them to CommandDefinitions
 * (which define how the slash command is invoked).
 */
@Service
public class CommandScanner {
	private static final Logger log = LoggerFactory.getLogger(CommandScanner.class);

	private final CommandRegistry commandRegistry;

	@Autowired
	public CommandScanner(CommandRegistry commandRegistry) {
		this.commandRegistry = commandRegistry;
	}

	/**
	 * Register exactly one command name in our command registry with a matching bean method.
	 */
	public void registerBeansInContext(ApplicationContext context, String commandName) {
		registerBeansInContext(context, Set.of(commandName));
	}

	/**
	 * Register all provided command names in our command registry with their matching bean methods.
	 */
	public void registerBeansInContext(ApplicationContext context, Set<String> commandNames) {
		if (commandNames == null || commandNames.isEmpty()) {
			log.debug("No command names provided. Skipping bean registration.");
			return;
		}

		Map<String, CommandBase> commandBeans = context.getBeansOfType(CommandBase.class);
		if (commandBeans.isEmpty()) {
			log.debug("No CommandBase beans found in context = {}", context.getDisplayName());
			return;
		}
		log.debug("Found {} CommandBase beans in context = {}", commandBeans.size(), context.getDisplayName());

		for (Map.Entry<String, CommandBase> beanEntry : commandBeans.entrySet()) {
			CommandBase beanInstance = beanEntry.getValue();

			Method commandMethod = findAnnotatedCommandMethod(beanInstance);
			if (commandMethod == null) {
				log.debug("Bean '{}' has no valid @Command-annotated method. Skipping.", beanEntry.getKey());
				continue;
			}

			// Get annotation details
			Command annotation =
					commandMethod.getAnnotation(Command.class);
			String declaredName = annotation.name();

			// Only proceed if declaredName is in the requested set
			if (!commandNames.contains(declaredName)) {
				log.debug("Command '{}' not in requested set. Skipping bean '{}'.", declaredName, beanEntry.getKey());
				continue;
			}

			// Already registered with a bean?
			if (commandRegistry.isRegistered(declaredName)) {
				log.debug("Command '{}' is already registered. Skipping bean '{}'.", declaredName, beanEntry.getKey());
				continue;
			}

			CommandDefinition definition = commandRegistry.get(declaredName);
			if (definition == null) {
				log.warn("No command configuration found for '{}'. Skipping bean '{}'.", declaredName, beanEntry.getKey());
				continue;
			}

			// Link the bean and its method into our definition
			definition.setBeanInstance(beanInstance);
			definition.setMethod(commandMethod);
			commandRegistry.update(definition);
		}
	}

	/**
	 * Finds the first method named 'onCommand' annotated with
	 *
	 * @me.whereareiam.yui.api.output.command.Command that has a single
	 * SlashCommandInteractionEvent parameter.
	 */
	private Method findAnnotatedCommandMethod(Object beanInstance) {
		for (Method m : beanInstance.getClass().getMethods()) {
			if (m.isAnnotationPresent(Command.class)
					&& "onCommand".equals(m.getName())) {
				Class<?>[] paramTypes = m.getParameterTypes();
				if (paramTypes.length == 1 && paramTypes[0] == SlashCommandInteractionEvent.class) {
					return m;
				}
			}
		}
		return null;
	}
}
