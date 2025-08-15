package me.whereareiam.yui.common.service.initialization.tasks;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.input.Registry;
import me.whereareiam.yui.api.input.UserRoleService;
import me.whereareiam.yui.api.output.LifecycleTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SyncRolesTask implements LifecycleTask {
	private final UserRoleService roles;
	private final Registry<LifecycleTask> lifecycleRegistry;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "SYNC_ROLES";
	}

	@Override
	public List<String> getDependencies() {
		return List.of("INIT_COMMANDS");
	}

	@Override
	public CompletableFuture<Void> start() {
		roles.syncAll();
		return CompletableFuture.completedFuture(null);
	}
}


