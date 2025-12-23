package me.whereareiam.yui.adapter.command.definition;

import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.type.Source;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory registry for {@link CommandDefinition} instances, tracking their source and containers.
 */
@Component
public class CommandDefinitionRegistry {
	public record Entry(String id, Source source, CommandDefinition definition) {}

	private final Map<String, Entry> definitions = new ConcurrentHashMap<>();
	private final Map<ApplicationContext, Set<String>> definitionIdsByContext = new ConcurrentHashMap<>();

	public @NotNull Optional<CommandDefinition> get(@NotNull String id) {
		return Optional.ofNullable(definitions.get(id)).map(Entry::definition);
	}

	public @NotNull Map<String, CommandDefinition> getAll() {
		return Collections.unmodifiableMap(definitions.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().definition())));
	}

	public void put(@NotNull String id, @NotNull String sourceId, @NotNull Source source, @NotNull CommandDefinition definition) {
		definitions.put(id, new Entry(sourceId, source, definition));
	}

	/**
	 * Associate a definition ID with an ApplicationContext for tracking.
	 * This allows proper cleanup when a plugin context is disabled.
	 *
	 * @param context the application context
	 * @param definitionId the definition ID to track
	 */
	public void trackDefinition(@NotNull ApplicationContext context, @NotNull String definitionId) {
		definitionIdsByContext.computeIfAbsent(context, _ -> ConcurrentHashMap.newKeySet()).add(definitionId);
	}

	/**
	 * Get all definition IDs associated with a specific context.
	 *
	 * @param context the application context
	 * @return unmodifiable set of definition IDs, or empty set if none found
	 */
	public @NotNull Set<String> getDefinitionIdsByContext(@NotNull ApplicationContext context) {
		Set<String> ids = definitionIdsByContext.get(context);
		return ids != null ? Set.copyOf(ids) : Set.of();
	}

	/**
	 * Remove all definitions associated with a specific context.
	 *
	 * @param context the application context
	 * @return the set of definition IDs that were removed
	 */
	public @NotNull Set<String> removeByContext(@NotNull ApplicationContext context) {
		Set<String> ids = definitionIdsByContext.remove(context);
		if (ids != null) {
			ids.forEach(definitions::remove);
			return Set.copyOf(ids);
		}

		return Set.of();
	}

	public void removeById(@NotNull String id) {
		definitions.remove(id);
		// Also remove from context tracking
		definitionIdsByContext.values().forEach(set -> set.remove(id));
	}

	public void removeByAlias(@NotNull String alias) {
		definitions.entrySet().removeIf(entry -> {
			CommandDefinition def = entry.getValue().definition();
			boolean matches = def.getAliases() != null && !def.getAliases().isEmpty()
					&& alias.equals(def.getAliases().getFirst());

			if (matches) {
				String id = entry.getKey();
				definitionIdsByContext.values().forEach(set -> set.remove(id));
			}
			return matches;
		});
	}

	public void removeBySource(@NotNull String id, @NotNull Source source) {
		definitions.entrySet().removeIf(entry -> {
			boolean matches = entry.getValue().source() == source && id.equals(entry.getValue().id());
			if (matches) {
				String defId = entry.getKey();
				definitionIdsByContext.values().forEach(set -> set.remove(defId));
			}

			return matches;
		});
	}
}
