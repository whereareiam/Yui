package me.whereareiam.yui.api.output;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Declarative application lifecycle task.
 * Supports startup (start) and shutdown (stop) phases using the same
 * dependency model to avoid duplicated orchestration code.
 */
public interface LifecycleTask {
	String getName();

	default Collection<String> getDependencies() {
		return List.of();
	}

	default boolean shouldRun() {
		return true;
	}

	CompletableFuture<Void> start();

	default CompletableFuture<Void> stop() {
		return CompletableFuture.completedFuture(null);
	}
}


