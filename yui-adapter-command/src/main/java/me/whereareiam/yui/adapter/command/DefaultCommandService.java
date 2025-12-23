package me.whereareiam.yui.adapter.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.adapter.command.definition.DefinitionProviderRegistry;
import me.whereareiam.yui.adapter.command.exception.DefaultExceptionHandlerRegistry;
import me.whereareiam.yui.adapter.command.parsing.definition.CommandDefinitionParser;
import me.whereareiam.yui.adapter.command.registration.AnnotationCommandRegistrar;
import me.whereareiam.yui.adapter.command.registration.CommandScanner;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.command.CommandService;
import me.whereareiam.yui.command.DefinitionProvider;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.command.exception.ExceptionResponse;
import me.whereareiam.yui.exception.command.base.CommandException;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.type.Source;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultCommandService implements CommandService, ApplicationListener<ContextClosedEvent> {
	private final JDA6CommandManager<Interaction> commandManager;
	private final CommandDefinitionRegistry definitionRegistry;
	private final DefinitionProviderRegistry definitionProviderRegistry;
	private final CommandScanner commandScanner;
	private final DefaultExceptionHandlerRegistry exceptionHandlerRegistry;

	private AnnotationCommandRegistrar<Interaction> registrar;

	private AnnotationCommandRegistrar<Interaction> registrar() {
		if (registrar == null) {
			CommandDefinitionParser<Interaction> definitionParser = new CommandDefinitionParser<>(commandManager);
			this.registrar = new AnnotationCommandRegistrar<>(definitionParser, Interaction.class, _ -> null);
		}

		return registrar;
	}

	@Override
	public void register(@NotNull ApplicationContext context) {
		DefinitionSnapshot snapshot = buildSnapshot();
		Collection<Object> containers = commandScanner.findCommand(context);
		
		// Get plugin ID if this is a plugin context
		String pluginId = getPluginId(context);
		
		log.debug("Registering {} command containers for context (pluginId: {})", containers.size(), pluginId);
		
		registerContainers(snapshot, containers);
		
		// Track definition IDs by context for proper cleanup
		for (Object container : containers) {
			Set<String> definitionIds = extractDefinitionIds(container.getClass());
			log.debug("Container {} has definition IDs: {}", container.getClass().getSimpleName(), definitionIds);
			definitionIds.forEach(id -> {
				// Namespace definition IDs for plugins
				String namespacedId = pluginId != null ? pluginId + ":" + id : id;
				definitionRegistry.trackDefinition(context, namespacedId);
				log.debug("Tracked definition: {}", namespacedId);
			});
		}
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
	public void onApplicationEvent(@NotNull ContextClosedEvent event) {
		ApplicationContext closed = event.getApplicationContext();
		log.debug("Context closed, unregistering commands for context {}", closed.getId());

		// Remove commands for this context and any child contexts that were tracked
		Set<ApplicationContext> toCleanup = definitionRegistry.trackedContexts();
		for (ApplicationContext ctx : toCleanup)
			if (isSameOrChildContext(ctx, closed))
				unregisterByContext(ctx);
	}

	@Override
	public void unregisterByContext(@NotNull ApplicationContext context) {
		Set<String> trackedIds = definitionRegistry.getDefinitionIdsByContext(context);
		if (trackedIds.isEmpty()) {
			log.debug("No commands to unregister for context");
			return;
		}

		// Get plugin ID to strip namespace prefix when looking up definitions
		String pluginId = getPluginId(context);
		
		for (String trackedId : trackedIds) {
			// Strip namespace prefix to get the actual definition ID
			String actualDefinitionId = trackedId;
			if (pluginId != null && trackedId.startsWith(pluginId + ":"))
				actualDefinitionId = trackedId.substring(pluginId.length() + 1);
			
			CommandDefinition definition = findDefinition(actualDefinitionId);
			if (definition != null && definition.getAliases() != null) {
				String finalDefinitionId = actualDefinitionId;
				definition.getAliases().forEach(alias -> {
					try {
						commandManager.deleteRootCommand(alias);
						log.debug("Deleted command '{}' from definition '{}'", alias, finalDefinitionId);
					} catch (Exception e) {
						log.warn("Failed to delete root command '{}' for definition '{}'", alias, finalDefinitionId, e);
					}
				});
			}
		}
		
		// Remove all definitions and context tracking in one call
		definitionRegistry.removeByContext(context);
		log.debug("Unregistered {} command definitions from context", trackedIds.size());
	}

	private Set<String> extractDefinitionIds(Class<?> targetClass) {
		Set<String> definitionIds = new HashSet<>();
		
		// Check class-level @Definition
		Definition classDefinition = AnnotationUtils.findAnnotation(targetClass, Definition.class);
		if (classDefinition != null) {
			definitionIds.add(classDefinition.value());
		}
		
		// Check method-level @Definition
		for (Method method : targetClass.getDeclaredMethods()) {
			Definition methodDefinition = AnnotationUtils.findAnnotation(method, Definition.class);
			if (methodDefinition != null) {
				definitionIds.add(methodDefinition.value());
			}
		}
		
		return definitionIds;
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

		AnnotationCommandRegistrar<Interaction> reg = registrar();
		reg.setDefinitionLookup(snapshot.definitions()::get);
		reg.register(snapshot.rootAlias(), containers.toArray());
	}

	private String getPluginId(ApplicationContext context) {
		try {
			return context.getBean("pluginId", String.class);
		} catch (Exception e) {
			return null; // Not a plugin context
		}
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

	private boolean isSameOrChildContext(ApplicationContext candidate, ApplicationContext parent) {
		if (candidate == parent) return true;

		ApplicationContext current = candidate.getParent();
		while (current != null) {
			if (current == parent) return true;
			current = current.getParent();
		}
		return false;
	}
}
