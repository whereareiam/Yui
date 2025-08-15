package me.whereareiam.yui.common.service.initialization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.output.LifecycleTask;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LifecycleOrchestrator {
	private final LifecycleTaskRegistry registry;

	@EventListener(ApplicationReadyEvent.class)
	public void onStartup() {
		run(true).join();
	}

	@EventListener(ContextClosedEvent.class)
	public void onShutdown() {
		run(false).join();
	}

	private CompletableFuture<Void> run(boolean startup) {
		Map<String, LifecycleTask> tasks = registry.getAllByName();
		if (tasks.isEmpty()) return CompletableFuture.completedFuture(null);

		List<String> order = topoSort(tasks);
		if (!startup) Collections.reverse(order); // shutdown in reverse dependency order

		return runInWaves(order, tasks, startup);
	}

	private List<String> topoSort(Map<String, LifecycleTask> tasks) {
		Map<String, Set<String>> deps = new HashMap<>();
		Map<String, Set<String>> rev = new HashMap<>();

		tasks.forEach((name, task) -> {
			Set<String> required = new HashSet<>(task.getDependencies());
			required.retainAll(tasks.keySet());
			deps.put(name, required);
			required.forEach(dep -> rev.computeIfAbsent(dep, _ -> new HashSet<>()).add(name));
		});

		Deque<String> queue = new ArrayDeque<>();
		deps.forEach((n, d) -> {
			if (d.isEmpty()) queue.add(n);
		});

		List<String> order = new ArrayList<>();
		while (!queue.isEmpty()) {
			String n = queue.removeFirst();
			order.add(n);
			for (String child : rev.getOrDefault(n, Set.of())) {
				Set<String> childDeps = deps.get(child);
				childDeps.remove(n);
				if (childDeps.isEmpty()) queue.add(child);
			}
		}

		if (order.size() != tasks.size()) {
			Set<String> cycle = new HashSet<>(tasks.keySet());
			order.forEach(cycle::remove);
			log.error("Lifecycle tasks contain a dependency cycle: {}", cycle);

			throw new IllegalStateException("Lifecycle task dependency cycle: " + cycle);
		}

		return order;
	}

	private CompletableFuture<Void> runInWaves(List<String> topoOrder, Map<String, LifecycleTask> tasks, boolean startup) {
		// compute level per node
		Map<String, Integer> level = new HashMap<>();
		Function<String, Integer> computeLevel = new Function<>() {
			@Override
			public Integer apply(String name) {
				if (level.containsKey(name)) return level.get(name);
				Set<String> deps = new HashSet<>(tasks.get(name).getDependencies());
				deps.retainAll(tasks.keySet());
				int lv = deps.isEmpty() ? 0 : deps.stream().map(this).mapToInt(Integer::intValue).max().orElse(0) + 1;
				level.put(name, lv);
				return lv;
			}
		};
		topoOrder.forEach(computeLevel::apply);

		Map<Integer, List<String>> byLevel = topoOrder.stream().collect(Collectors.groupingBy(level::get, LinkedHashMap::new, Collectors.toList()));
		if (!startup) {
			// reverse within each wave for shutdown for consistency
			for (List<String> v : byLevel.values()) Collections.reverse(v);
			// and reverse wave order
			List<Map.Entry<Integer, List<String>>> entries = new ArrayList<>(byLevel.entrySet());
			Collections.reverse(entries);
			byLevel = new LinkedHashMap<>(entries.size());
			for (Map.Entry<Integer, List<String>> e : entries) byLevel.put(e.getKey(), e.getValue());
		}

		CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
		for (List<String> wave : byLevel.values()) {
			final boolean isStartup = startup;
			chain = chain.thenCompose((Function<Void, CompletableFuture<Void>>) v -> runWave(wave, tasks, isStartup));
		}
		return chain;
	}

	private CompletableFuture<Void> runWave(List<String> names, Map<String, LifecycleTask> tasks, boolean startup) {
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (String n : names) {
			LifecycleTask t = tasks.get(n);

			if (t == null) continue;
			if (!t.shouldRun()) continue;

			log.debug("[Lifecycle] {} task: {}", startup ? "Starting" : "Stopping", n);
			try {
				CompletableFuture<Void> f = startup ? t.start() : t.stop();
				futures.add(f.exceptionally(ex -> {
					log.error("[Lifecycle] Task failed: {}", n, ex);
					return null;
				}));
			} catch (Exception ex) {
				log.error("[Lifecycle] Task threw before {}: {}", startup ? "start" : "stop", n, ex);
			}
		}

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}
}


