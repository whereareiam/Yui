package me.whereareiam.yui.adapter.command;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InitializeCommandsTask implements LifecycleTask {
	private final Registry<LifecycleTask> lifecycleRegistry;
	private final CommandService commandService;
	private final ApplicationContext ctx;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "INIT_COMMANDS";
	}

	@Override
	public List<String> getDependencies() {
		return List.of("INIT_TRANSLATIONS");
	}

	@Override
	public CompletableFuture<Void> start() {
		commandService.register(ctx);
		return CompletableFuture.completedFuture(null);
	}
}

