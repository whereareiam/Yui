package me.whereareiam.yui.common.initialization.tasks;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.plugin.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
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
		long enabled = pluginManager.plugins().stream().filter(InternalPlugin::isEnabled).count();
		log.info("Loaded {} plugin{}", enabled, enabled == 1 ? "" : "s");
		log.info("");
		return CompletableFuture.completedFuture(null);
	}
}


