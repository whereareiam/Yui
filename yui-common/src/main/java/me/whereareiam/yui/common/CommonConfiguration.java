package me.whereareiam.yui.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.input.UserRoleService;
import me.whereareiam.yui.api.input.translation.TranslationService;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import me.whereareiam.yui.api.output.plugin.PluginManager;
import me.whereareiam.yui.api.output.service.CommandService;
import me.whereareiam.yui.common.scanner.ComponentListenerScanner;
import me.whereareiam.yui.common.scanner.ListenerScanner;
import me.whereareiam.yui.common.service.DefaultTemporaryChannelService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommonConfiguration {
	private final ApplicationContext ctx;

	@Bean
	@Primary
	public JDA jda(Settings settings) {
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

			jda.getGuilds()
					.stream()
					.filter(guild -> !guild.getId().equals(settings.getDiscord().getGuildId()))
					.forEach(guild -> {
						log.info("Leaving guild '{}' ({})", guild.getName(), guild.getId());
						guild.leave().queue();
					});


		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return jda;
	}

	@Bean(destroyMethod = "shutdown")
	public ExecutorService syncPool() {
		return Executors.newFixedThreadPool(
				Math.max(2, Runtime.getRuntime().availableProcessors()),
				r -> new Thread(r, "yui-role-sync")
		);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		ctx.getBean(DefaultTemporaryChannelService.class).purgeChannels()
				.thenRun(() -> {
					ctx.getBean(CommandService.class).initialize();
					ctx.getBean(TranslationService.class).initialize();
					ctx.getBean(UserRoleService.class).syncAll();
					ctx.getBean(PluginManager.class).initialize();

					ctx.getBean(ComponentListenerScanner.class).scan();
					ctx.getBean(ListenerScanner.class).scan();

					welcome();
				})
				.exceptionally(ex -> {
					log.error("Error during channel purge", ex);
					return null;
				});
	}

	private void welcome() {
		log.info("");
		log.info("Yui has successfully linked with the Cardinal System.");
		log.info("『Greetings, Master. I am Yui. All systems are operational. Awaiting your command.』");
		log.info("");
		log.info("Loaded {} plugin{}", ctx.getBean(PluginManager.class).plugins().stream().filter(InternalPlugin::isEnabled).count(), ctx.getBean(PluginManager.class).plugins().size() == 1 ? "" : "s");
		log.info("");
	}
}
