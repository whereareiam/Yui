package me.whereareiam.yui.adapter.command.definition;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.DefinitionProvider;
import me.whereareiam.yui.type.Source;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Aggregates all {@link DefinitionProvider} beans and produces a merged view.
 */
@Component
@RequiredArgsConstructor
public class DefinitionProviderRegistry {
	private final ObjectProvider<DefinitionProvider> providers;
	private final Map<String, DefinitionProvider> externalProviders = new ConcurrentHashMap<>();

	private static final Map<Source, Integer> SOURCE_PRIORITY = Map.of(
			Source.INTERNAL, 0,
			Source.EXTERNAL, 1
	);

	public record ProviderEntry(
			String id,
			Source source,
			CommandDefinition definition
	) {}

	/**
	 * Merge definitions from all providers, applying a deterministic priority:
	 * INTERNAL before EXTERNAL.
	 */
	public Map<String, ProviderEntry> merged() {
		List<DefinitionProvider> ordered = Stream.concat(
						providers.orderedStream(),
						externalProviders.values().stream()
				)
				.sorted(Comparator.comparingInt(p -> SOURCE_PRIORITY.getOrDefault(p.source(), 0)))
				.toList();

		Map<String, ProviderEntry> merged = new LinkedHashMap<>();
		for (DefinitionProvider provider : ordered) {
			provider.definitions().forEach((id, def) -> merged.put(id,
					new ProviderEntry(provider.id(), provider.source(), def)));
		}
		return merged;
	}

	public void addExternalProvider(@NotNull DefinitionProvider provider) {
		externalProviders.put(provider.id(), provider);
	}

	public void removeExternalProvider(@NotNull String sourceId) {
		externalProviders.remove(sourceId);
	}
}

