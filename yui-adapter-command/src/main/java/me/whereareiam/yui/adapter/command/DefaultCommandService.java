package me.whereareiam.yui.adapter.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.adapter.command.definition.DefinitionProviderRegistry;
import me.whereareiam.yui.adapter.command.parsing.definition.CommandDefinitionParser;
import me.whereareiam.yui.adapter.command.registration.CommandScanner;
import me.whereareiam.yui.adapter.command.registration.AnnotationCommandRegistrar;
import me.whereareiam.yui.exception.command.base.CommandException;
import me.whereareiam.yui.adapter.command.exception.DefaultExceptionHandlerRegistry;
import me.whereareiam.yui.command.exception.ExceptionResponse;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.command.CommandService;
import me.whereareiam.yui.command.DefinitionProvider;
import me.whereareiam.yui.type.Source;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultCommandService implements CommandService {
	private final JDA6CommandManager<JDAInteraction> commandManager;
	private final CommandDefinitionRegistry definitionRegistry;
	private final DefinitionProviderRegistry definitionProviderRegistry;
	private final CommandScanner commandScanner;
	private final DefaultExceptionHandlerRegistry exceptionHandlerRegistry;

	private AnnotationCommandRegistrar<JDAInteraction> registrar;

	private AnnotationCommandRegistrar<JDAInteraction> registrar() {
		if (registrar == null) {
			CommandDefinitionParser<JDAInteraction> definitionParser = new CommandDefinitionParser<>(commandManager);
			this.registrar = new AnnotationCommandRegistrar<>(definitionParser, JDAInteraction.class, _ -> null);
		}

		return registrar;
	}

	@Override
	public void register(@NotNull ApplicationContext context) {
		DefinitionSnapshot snapshot = buildSnapshot();
		registerContainers(snapshot, commandScanner.findCommand(context));
	}

	@Override
	public void register(@NotNull Object command) {
		DefinitionSnapshot snapshot = buildSnapshot();
		registerContainers(snapshot, List.of(command));
	}

	@Override
	public void registerProvider(@NotNull DefinitionProvider provider) {
		definitionProviderRegistry.addExternalProvider(provider);
	}

	@Override
	public void register(@NotNull Class<?> commandClass) {
		register(commandScanner.findCommand(commandClass));
	}

	@Override
	public <T extends CommandException> void registerExceptionHandler(
			@NotNull Class<T> exceptionType,
			@NotNull Function<T, ExceptionResponse> handler
	) {
		exceptionHandlerRegistry.register(exceptionType, handler);
	}

	@Override
	public void unregisterByDefinitionId(@NotNull String definitionId) {
		CommandDefinition definition = findDefinition(definitionId);
		if (definition != null && definition.getAliases() != null) {
			definition.getAliases().forEach(alias -> {
				try {
					commandManager.deleteRootCommand(alias);
				} catch (Exception e) {
					log.warn("Failed to delete root command '{}' for definition '{}'", alias, definitionId, e);
				}
			});
		}

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
	public void unregisterProvider(@NotNull String id) {
		definitionProviderRegistry.removeExternalProvider(id);
		definitionRegistry.removeBySource(id, Source.EXTERNAL);
	}

	@Override
	public int getCommandCount() {
		return commandManager.commands().size();
	}

	@Override
	public @NotNull Map<String, CommandDefinition> getDefinitions() {
		return definitionRegistry.getAll();
	}

	private void registerContainers(DefinitionSnapshot snapshot, Collection<Object> containers) {
		if (containers == null || containers.isEmpty()) return;

		AnnotationCommandRegistrar<JDAInteraction> reg = registrar();
		reg.setDefinitionLookup(snapshot.definitions()::get);
		reg.register(snapshot.rootAlias(), containers.toArray());
	}

	private DefinitionSnapshot buildSnapshot() {
		Map<String, DefinitionProviderRegistry.ProviderEntry> mergedEntries = definitionProviderRegistry.merged();
		Map<String, CommandDefinition> mergedDefinitions = new LinkedHashMap<>();

		// Persist merged view into the registry with source info
		for (Map.Entry<String, DefinitionProviderRegistry.ProviderEntry> entry : mergedEntries.entrySet()) {
			String id = entry.getKey();
			DefinitionProviderRegistry.ProviderEntry providerEntry = entry.getValue();
			mergedDefinitions.put(id, providerEntry.definition());
			definitionRegistry.put(id, providerEntry.id(), providerEntry.source(), providerEntry.definition());
		}

		String rootAlias = selectRootAlias(mergedDefinitions);
		return new DefinitionSnapshot(Map.copyOf(mergedDefinitions), rootAlias);
	}

	private String selectRootAlias(Map<String, CommandDefinition> definitions) {
		CommandDefinition main = definitions.get("main");
		if (main == null || !main.isEnabled()) return null;

		return firstNonBlankAlias(main.getAliases());
	}

	private String firstNonBlankAlias(java.util.List<String> aliases) {
		if (aliases == null) return null;

		for (String alias : aliases) {
			if (alias == null) continue;
			String trimmed = alias.trim();
			if (!trimmed.isEmpty()) return trimmed;
		}

		return null;
	}

	private CommandDefinition findDefinition(String id) {
		return definitionRegistry.get(id).orElse(null);
	}

	private record DefinitionSnapshot(Map<String, CommandDefinition> definitions, String rootAlias) {}
}
