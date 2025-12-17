package me.whereareiam.yui.adapter.plugin.dependency;

import me.whereareiam.yui.model.plugin.Dependency;
import me.whereareiam.yui.model.plugin.Plugin;

import java.util.*;

/**
 * Resolves plugin load order based on declared dependencies.
 */
public final class PluginDependencyResolver {
	public static List<String> resolveLoadOrder(Map<String, Plugin> descriptorsById) {
		Map<String, List<String>> adjacency = new HashMap<>();
		Set<String> skipped = new HashSet<>();

		descriptorsById.forEach((id, plugin) -> {
			List<String> deps = new ArrayList<>();
			List<Dependency> declared = plugin.getDependencies();
			if (declared != null) {
				for (Dependency d : declared) {
					if (!descriptorsById.containsKey(d.getId())) {
						if (d.isRequired()) skipped.add(id);
						continue;
					}
					deps.add(d.getId());
				}
			}
			adjacency.put(id, deps);
		});

		List<String> output = new ArrayList<>();
		Set<String> visiting = new HashSet<>();
		Set<String> visited = new HashSet<>();

		for (String id : adjacency.keySet()) {
			if (skipped.contains(id) || visited.contains(id)) continue;
			if (hasCycle(id, adjacency, visiting, visited, output))
				return Collections.emptyList();
		}

		return output.stream().filter(id -> !skipped.contains(id)).toList();
	}

	private static boolean hasCycle(
			String id, Map<String, List<String>> g,
			Set<String> visiting,
			Set<String> visited,
			List<String> out
	) {
		if (visiting.contains(id)) return true;
		if (visited.contains(id)) return false;

		visiting.add(id);
		for (String dep : g.getOrDefault(id, List.of()))
			if (hasCycle(dep, g, visiting, visited, out)) return true;

		visiting.remove(id);
		visited.add(id);
		out.add(id);

		return false;
	}
}


