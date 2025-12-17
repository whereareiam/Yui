package me.whereareiam.yui.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.reader.ConfigReader;
import me.whereareiam.configura.type.Format;
import me.whereareiam.configura.writer.ConfigWriter;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.common.config.adapter.ColorAdapter;
import me.whereareiam.yui.common.config.adapter.DiscordLocaleAdapter;
import me.whereareiam.yui.config.ConfigurationTypeResolver;
import me.whereareiam.yui.registry.Registry;
import me.whereareiam.yui.type.ConfigurationType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ConfiguraBootstrap implements LifecycleTask {
	private final ConfigurationTypeResolver resolver;
	private final Registry<LifecycleTask> lifecycleRegistry;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "BOOTSTRAP_CONFIGURA";
	}

	@Override
	public CompletableFuture<Void> start() {
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

		return CompletableFuture.completedFuture(null);
	}
}

