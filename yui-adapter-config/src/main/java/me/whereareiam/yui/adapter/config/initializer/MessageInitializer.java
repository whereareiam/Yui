package me.whereareiam.yui.adapter.config.initializer;

import me.whereareiam.yui.model.config.messages.Messages;
import me.whereareiam.yui.config.ConfigurationLoader;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class MessageInitializer {
	@Autowired
	public MessageInitializer(@Qualifier("languagesPath") Path languagesPath, ConfigurationLoader configLoader) {
		configLoader.load(languagesPath.resolve(DiscordLocale.ENGLISH_US.getLocale()), Messages.class);
	}
}
