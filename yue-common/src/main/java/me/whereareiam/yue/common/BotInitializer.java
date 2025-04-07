package me.whereareiam.yue.common;

import me.whereareiam.yue.api.model.config.settings.Settings;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class BotInitializer {
	private final Settings settings;

	private final Logger logger = LoggerFactory.getLogger(BotInitializer.class);

	@Autowired
	public BotInitializer(Settings settings) {
		this.settings = settings;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initialize() {
		try {
			JDABuilder builder = JDABuilder
					.createDefault(settings.getDiscord().getToken())
					.setEnabledIntents(settings.getDiscord().getIntents())
					.setChunkingFilter(ChunkingFilter.ALL);

			builder.build().awaitReady();

			logger.info("");
			logger.info("Yue has successfully linked with the Cardinal System.");
			logger.info("『Greetings, Master. I am Yue. All systems are operational. Awaiting your command.』");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
