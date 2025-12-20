package me.whereareiam.yui.adapter.command.definition;

import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.type.Source;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry for {@link CommandDefinition} instances, tracking their source.
 */
@Component
public class CommandDefinitionRegistry {
	public record Entry(String id, Source source, CommandDefinition definition) {}

	private final Map<String, Entry> definitions = new ConcurrentHashMap<>();

	public @NotNull Optional<CommandDefinition> get(@NotNull String id) {
		return Optional.ofNullable(definitions.get(id)).map(Entry::definition);
	}

	public @NotNull Map<String, CommandDefinition> getAll() {
		return Collections.unmodifiableMap(definitions.entrySet().stream()
				.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> e.getValue().definition())));
	}

	public void put(@NotNull String id, @NotNull String sourceId, @NotNull Source source, @NotNull CommandDefinition definition) {
		definitions.put(id, new Entry(sourceId, source, definition));
	}

	public void removeById(@NotNull String id) {
		definitions.remove(id);
	}

	public void removeByAlias(@NotNull String alias) {
		definitions.entrySet().removeIf(entry -> {
			CommandDefinition def = entry.getValue().definition();
			return def.getAliases() != null && !def.getAliases().isEmpty()
					&& alias.equals(def.getAliases().getFirst());
		});
	}

	public void removeBySource(@NotNull String id, @NotNull Source source) {
		definitions.entrySet().removeIf(entry -> entry.getValue().source() == source
				&& id.equals(entry.getValue().id()));
	}
}
