package me.whereareiam.yui.common.initialization;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.common.conversation.open.ChannelOpener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class PurgeTempChannelsTask implements LifecycleTask {
	private final ChannelOpener channels;
	private final Registry<LifecycleTask> lifecycleRegistry;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "PURGE_TEMP_CHANNELS";
	}

	@Override
	public CompletableFuture<Void> start() {
		return channels.purge();
	}
}

