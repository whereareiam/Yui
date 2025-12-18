package me.whereareiam.yui.adapter.command;

import me.whereareiam.yui.model.command.CommandDefinition;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory registry for {@link CommandDefinition} instances
 * used by the command adapter module.
 */
@Component
public class CommandDefinitionRegistry {
	private final Map<String, CommandDefinition> definitions = new ConcurrentHashMap<>();

	public @NotNull Optional<CommandDefinition> get(@NotNull String id) {
		return Optional.ofNullable(definitions.get(id));
	}

	public @NotNull Map<String, CommandDefinition> getAll() {
		return Collections.unmodifiableMap(definitions);
	}

	public void put(@NotNull String id, @NotNull CommandDefinition definition) {
		definitions.put(id, definition);
	}

	public void putAll(@NotNull Map<String, CommandDefinition> defs) {
		definitions.putAll(defs);
	}

	public void removeById(@NotNull String id) {
		definitions.remove(id);
	}

	public void removeByAlias(@NotNull String alias) {
		definitions.entrySet().removeIf(entry -> {
			CommandDefinition def = entry.getValue();
			return def.getAliases() != null && !def.getAliases().isEmpty()
					&& alias.equals(def.getAliases().getFirst());
		});
	}
}
