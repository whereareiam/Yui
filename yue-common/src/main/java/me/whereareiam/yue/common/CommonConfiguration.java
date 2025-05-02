package me.whereareiam.yue.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yue.api.input.UserRoleService;
import me.whereareiam.yue.api.input.translation.TranslationService;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.model.plugin.InternalPlugin;
import me.whereareiam.yue.api.output.plugin.PluginManager;
import me.whereareiam.yue.api.output.service.CommandService;
import me.whereareiam.yue.common.scanner.ComponentListenerScanner;
import me.whereareiam.yue.common.scanner.ListenerScanner;
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

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		ctx.getBean(CommandService.class).initialize();
		ctx.getBean(PluginManager.class).initialize();
		ctx.getBean(TranslationService.class).initialize();

		ctx.getBean(ComponentListenerScanner.class).scan();
		ctx.getBean(ListenerScanner.class).scan();
		ctx.getBean(UserRoleService.class).syncAll();

		welcome();
	}

	private void welcome() {
		log.info("");
		log.info("Yue has successfully linked with the Cardinal System.");
		log.info("『Greetings, Master. I am Yue. All systems are operational. Awaiting your command.』");
		log.info("");
		log.info("Loaded {} plugin{}", ctx.getBean(PluginManager.class).plugins().stream().filter(InternalPlugin::isEnabled).count(), ctx.getBean(PluginManager.class).plugins().size() == 1 ? "" : "s");
		log.info("");
	}
}
