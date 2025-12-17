package me.whereareiam.yui.adapter.command.initialization;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InitializeCommandsTask implements LifecycleTask {
	private final CommandService commands;
	private final Registry<LifecycleTask> lifecycleRegistry;

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
		if (commands instanceof Reloadable r) r.reload();

		return CompletableFuture.completedFuture(null);
	}
}


