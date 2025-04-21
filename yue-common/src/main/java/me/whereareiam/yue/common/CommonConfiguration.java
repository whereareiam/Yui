package me.whereareiam.yue.common;

import me.whereareiam.yue.api.StyleKit;
import me.whereareiam.yue.api.input.translation.TranslationService;
import me.whereareiam.yue.api.model.config.settings.Settings;
import me.whereareiam.yue.api.model.config.style.Palette;
import me.whereareiam.yue.api.model.config.style.embed.EmbedStyle;
import me.whereareiam.yue.api.output.plugin.PluginService;
import me.whereareiam.yue.api.output.service.CommandService;
import me.whereareiam.yue.common.scanner.ComponentListenerScanner;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;

@Configuration
public class CommonConfiguration {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ApplicationContext ctx;

	@Autowired
	public CommonConfiguration(ApplicationContext ctx) {
		this.ctx = ctx;
	}

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

	@Autowired
	public void setup(Palette palette, EmbedStyle embeds) {
		StyleKit.init(
				palette,
				embeds
		);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		ctx.getBean(PluginService.class).loadPlugins();
		ctx.getBean(TranslationService.class).initialize();
		ctx.getBean(CommandService.class).initialize();
		ctx.getBean(ComponentListenerScanner.class).scan();

		welcome();
	}

	private void welcome() {
		logger.info("");
		logger.info("Yue has successfully linked with the Cardinal System.");
		logger.info("『Greetings, Master. I am Yue. All systems are operational. Awaiting your command.』");
		logger.info("");
	}
}
