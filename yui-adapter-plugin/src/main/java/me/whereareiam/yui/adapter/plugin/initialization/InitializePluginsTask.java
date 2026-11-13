package me.whereareiam.yui.adapter.plugin.initialization;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.adapter.plugin.PluginHost;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class InitializePluginsTask implements LifecycleTask {
	private final PluginHost plugins;
	private final Registry<LifecycleTask> lifecycleRegistry;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "INIT_PLUGINS";
	}

	@Override
	public List<String> getDependencies() {
		return List.of("SYNC_ROLES");
	}

	@Override
	public CompletableFuture<Void> start() {
		plugins.start();
		return CompletableFuture.completedFuture(null);
	}
}


