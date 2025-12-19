package me.whereareiam.yui.service;

import me.whereareiam.yui.DefinitionProvider;
import me.whereareiam.yui.model.command.CommandDefinition;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;

import java.util.Map;

@SuppressWarnings("unused")
public interface CommandService {
	/**
	 * Register all command containers found in the given Spring {@link ApplicationContext}.
	 * <p>
	 * A command container is any bean whose concrete class is annotated with
	 * {@code @me.whereareiam.yui.annotation.command.Command} at the type level.
	 * Method-level annotations are discovered by the underlying annotation parser.
	 */
	void register(@NotNull ApplicationContext context);

	/**
	 * Register all command containers in the given context, first adding a single
	 * {@link CommandDefinition} under the provided {@code definitionId}.
	 * <p>
	 * If a definition with the same id already exists, it will be replaced.
	 */
	void register(@NotNull Object commandContainer);

	/**
	 * Register a command container by its class, resolved from the Spring context.
	 */
	void register(@NotNull Class<?> commandClass);

	/**
	 * Register definitions via an external provider (plugins or API users).
	 */
	void registerProvider(@NotNull DefinitionProvider provider);

	/**
	 * Unregister an external provider by its source id.
	 */
	void unregisterProvider(@NotNull String sourceId);

	/**
	 * Remove a {@link CommandDefinition} from the registry by its id.
	 * <p>
	 * This affects future registrations that rely on this definition. Existing
	 * commands may remain registered until a rebuild or restart.
	 */
	void unregisterByDefinitionId(@NotNull String definitionId);

	/**
	 * Remove any registered definitions whose primary (first) alias matches the given {@code alias}.
	 * <p>
	 * This is a best-effort helper for unregistration by user-facing command name.
	 */
	void unregisterByAlias(@NotNull String alias);

	/**
	 * Returns the number of commands currently registered with the underlying command manager.
	 */
	int getCommandCount();

	/**
	 * Returns an immutable view of all {@link CommandDefinition} instances that have been
	 * registered programmatically through this service.
	 */
	@NotNull Map<String, CommandDefinition> getDefinitions();
}
