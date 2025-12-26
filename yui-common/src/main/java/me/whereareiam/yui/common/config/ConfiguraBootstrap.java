package me.whereareiam.yui.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.reader.ConfigReader;
import me.whereareiam.configura.type.Format;
import me.whereareiam.configura.writer.ConfigWriter;
import me.whereareiam.yui.common.adapter.DurationAdapter;
import me.whereareiam.yui.common.config.adapter.ColorAdapter;
import me.whereareiam.yui.common.config.adapter.DiscordLocaleAdapter;
import me.whereareiam.yui.config.ConfigurationTypeResolver;
import me.whereareiam.yui.model.type.Duration;
import me.whereareiam.yui.type.ConfigurationType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@RequiredArgsConstructor
public class ConfiguraBootstrap {
	private final ConfigurationTypeResolver resolver;

	@PostConstruct
	public void initConfigura() {
		// Resolve the preferred configuration format
		ConfigurationType type = resolver.getConfigurationType();
		Format format = (type == ConfigurationType.JSON) ? Format.JSON : Format.YAML;

		// Configure global reader/writer with chosen format
		ConfigReader reader = Config.reader(format);
		ConfigWriter writer = Config.writer(format);
		Config.setReader(reader);
		Config.setWriter(writer);

		// Register adapters
		Config.registerAdapter(Color.class, ColorAdapter.class);
		Config.registerAdapter(DiscordLocale.class, DiscordLocaleAdapter.class);
		Config.registerAdapter(Duration.class, DurationAdapter.class);
	}
}
