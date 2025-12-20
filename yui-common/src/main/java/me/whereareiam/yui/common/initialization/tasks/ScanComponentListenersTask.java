package me.whereareiam.yui.common.initialization.tasks;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.common.scanner.ComponentListenerScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ScanComponentListenersTask implements LifecycleTask {
	private final ComponentListenerScanner scanner;
	private final ApplicationContext ctx;
	private final Registry<LifecycleTask> lifecycleRegistry;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "SCAN_COMPONENT_LISTENERS";
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


