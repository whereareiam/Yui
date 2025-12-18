package me.whereareiam.yui.common;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.model.config.settings.Settings;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommonConfiguration {
	private final ObjectProvider<Settings> settingsProvider;

	@Bean
	@Primary
	public JDA jda() {
		Settings settings = settingsProvider.getObject();
		JDA jda;

		try {
			if (settings.getDiscord().getGuildId().equals("SET_YOUR_GUILD_ID"))
				throw new IllegalStateException("Discord guild id is not set");

			if (settings.getDiscord().getToken().equals("SET_YOUR_TOKEN"))
				throw new IllegalStateException("Discord token is not set");

			JDABuilder builder = JDABuilder
					.createDefault(settings.getDiscord().getToken())
					.setEnabledIntents(settings.getDiscord().getIntents())
					.setChunkingFilter(ChunkingFilter.ALL)
					.setMemberCachePolicy(MemberCachePolicy.ALL);

			jda = builder.build().awaitReady();

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return jda;
	}

	@PreDestroy
	public void shutdownJda(JDA jda) {
		if (jda == null) return;

		log.info("Shutting down YUI");
		jda.shutdown();
	}

	@Bean(destroyMethod = "shutdown")
	public ExecutorService syncPool() {
		return Executors.newFixedThreadPool(
				Math.max(2, Runtime.getRuntime().availableProcessors()),
				r -> new Thread(r, "yui-role-sync")
		);
	}
}
