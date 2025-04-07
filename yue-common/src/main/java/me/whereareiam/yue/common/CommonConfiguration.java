package me.whereareiam.yue.common;

import me.whereareiam.yue.api.model.config.settings.Settings;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CommonConfiguration {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	@Primary
	public JDA jda(Settings settings) {
		JDA jda;

		try {
			JDABuilder builder = JDABuilder
					.createDefault(settings.getDiscord().getToken())
					.setEnabledIntents(settings.getDiscord().getIntents())
					.setChunkingFilter(ChunkingFilter.ALL);

			jda = builder.build().awaitReady();

			logger.info("");
			logger.info("Yue has successfully linked with the Cardinal System.");
			logger.info("『Greetings, Master. I am Yue. All systems are operational. Awaiting your command.』");
			logger.info("");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return jda;
	}
}
