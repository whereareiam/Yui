package me.whereareiam.yui.adapter.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.command.registration.CommandDefinitionParser;
import me.whereareiam.yui.adapter.command.registration.CommandScanner;
import me.whereareiam.yui.adapter.command.registration.annotation.AnnotationCommandRegistrar;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.service.CommandService;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultCommandService implements CommandService {
	private final JDA6CommandManager<JDAInteraction> commandManager;
	private final CommandDefinitionRegistry definitionRegistry;
	private final CommandScanner commandScanner;

	private AnnotationCommandRegistrar<JDAInteraction> registrar;

	private AnnotationCommandRegistrar<JDAInteraction> registrar() {
		if (registrar == null) {
			CommandDefinitionParser<JDAInteraction> definitionParser = new CommandDefinitionParser<>(commandManager);
			Function<String, CommandDefinition> definitionLookup = id -> definitionRegistry.get(id).orElse(null);
			this.registrar = new AnnotationCommandRegistrar<>(definitionParser, JDAInteraction.class, definitionLookup);
		}

		return registrar;
	}

	@Override
	public void register(@NotNull ApplicationContext context) {
		Collection<Object> containers = commandScanner.findCommand(context);
		if (containers.isEmpty()) return;

		registrar().register(containers.toArray());
	}

	@Override
	public void register(
			@NotNull ApplicationContext context,
			@NotNull String definitionId,
			@NotNull CommandDefinition definition
	) {
		definitionRegistry.put(definitionId, definition);
		register(context);
	}

	@Override
	public void register(
			@NotNull ApplicationContext context,
			@NotNull Map<String, CommandDefinition> definitions
	) {
		definitionRegistry.putAll(definitions);
		register(context);
	}

	@Override
	public void register(@NotNull Object command) {
		registrar().register(command);
	}

	@Override
	public void register(
			@NotNull Object command,
			@NotNull String definitionId,
			@NotNull CommandDefinition definition
	) {
		definitionRegistry.put(definitionId, definition);
		register(command);
	}

	@Override
	public void register(
			@NotNull Object command,
			@NotNull Map<String, CommandDefinition> definitions
	) {
		definitionRegistry.putAll(definitions);
		register(command);
	}

	@Override
	public void unregisterByDefinitionId(@NotNull String definitionId) {
		definitionRegistry.get(definitionId).ifPresent(def -> {
			if (def.getAliases() != null) {
				def.getAliases().forEach(alias -> {
					try {
						commandManager.deleteRootCommand(alias);
					} catch (Exception e) {
						log.warn("Failed to delete root command '{}' for definition '{}'", alias, definitionId, e);
					}
				});
			}
		});

		definitionRegistry.removeById(definitionId);
		log.debug("Unregistered command definition '{}'", definitionId);
	}

	@Override
	public void unregisterByAlias(@NotNull String alias) {
		try {
			commandManager.deleteRootCommand(alias);
		} catch (Exception e) {
			log.warn("Failed to delete root command for alias '{}'", alias, e);
		}

		definitionRegistry.removeByAlias(alias);
		log.debug("Requested unregistration by alias '{}'", alias);
	}

	@Override
	public int getCommandCount() {
		return commandManager.commands().size();
	}

	@Override
	public @NotNull Map<String, CommandDefinition> getDefinitions() {
		return definitionRegistry.getAll();
	}
}

