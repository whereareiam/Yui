package me.whereareiam.yui.common.service.initialization.tasks;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.common.service.DefaultTranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InitializeTranslationsTask implements LifecycleTask {
	private final DefaultTranslationService translations;
	private final Registry<LifecycleTask> lifecycleRegistry;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "INIT_TRANSLATIONS";
	}

	@Override
	public List<String> getDependencies() {
		return List.of("PURGE_TEMP_CHANNELS");
	}

	@Override
	public CompletableFuture<Void> start() {
		translations.initialize();
		return CompletableFuture.completedFuture(null);
	}
}


