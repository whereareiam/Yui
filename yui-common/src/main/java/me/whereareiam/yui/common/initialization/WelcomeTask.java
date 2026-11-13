package me.whereareiam.yui.common.initialization;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.chassis.type.ExtensionState;
import me.whereareiam.yui.api.LifecycleTask;
import me.whereareiam.yui.api.Registry;
import me.whereareiam.yui.api.plugin.PluginManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeTask implements LifecycleTask {
	private final PluginManager pluginManager;
	private final Registry<LifecycleTask> lifecycleRegistry;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "WELCOME";
	}

	@Override
	public List<String> getDependencies() {
		return List.of("SCAN_COMPONENT_LISTENERS", "SCAN_JDA_LISTENERS");
	}

	@Override
	public CompletableFuture<Void> start() {
		log.info("");
		log.info("Yui has successfully linked with the Cardinal System.");
		log.info("『Greetings, Master. I am Yui. All systems are operational. Awaiting your command.』");
		log.info("");
		long enabled = pluginManager.plugins().stream().filter(plugin -> plugin.getState() == ExtensionState.ENABLED).count();
		log.info("Loaded {} plugin{}", enabled, enabled == 1 ? "" : "s");
		log.info("");
		return CompletableFuture.completedFuture(null);
	}
}


