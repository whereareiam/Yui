package me.whereareiam.yui.common.initialization;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.Registry;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LeaveUnwantedGuildsTask implements LifecycleTask {
	private final ObjectProvider<Settings> settingsProvider;
	private final Registry<LifecycleTask> lifecycleRegistry;
	private final JDA jda;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "LEAVE_UNWANTED_GUILDS";
	}

	@Override
	public CompletableFuture<Void> start() {
		Settings settings = settingsProvider.getObject();
		String targetGuildId = settings.getDiscord().getGuildId();

		jda.getGuilds()
				.stream()
				.filter(guild -> !guild.getId().equals(targetGuildId))
				.forEach(guild -> {
					log.info("Leaving guild '{}' ({})", guild.getName(), guild.getId());
					guild.leave().queue();
				});

		return CompletableFuture.completedFuture(null);
	}
}
