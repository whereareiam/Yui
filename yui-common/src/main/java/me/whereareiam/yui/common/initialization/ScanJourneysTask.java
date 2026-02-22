package me.whereareiam.yui.common.initialization;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.common.scanner.JourneyAnnotationScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ScanJourneysTask implements LifecycleTask {
	private final JourneyAnnotationScanner scanner;
	private final ApplicationContext ctx;
	private final Registry<LifecycleTask> lifecycleRegistry;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "SCAN_JOURNEYS";
	}

	@Override
	public List<String> getDependencies() {
		return List.of("INIT_PLUGINS");
	}

	@Override
	public CompletableFuture<Void> start() {
		scanner.scan(ctx);
		return CompletableFuture.completedFuture(null);
	}
}
