package me.whereareiam.yui.common;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.semantica.Semantica;
import me.whereareiam.semantica.SemanticaConfiguration;
import me.whereareiam.semantica.locale.LocaleParser;
import me.whereareiam.semantica.model.SemanticLocale;
import me.whereareiam.semantica.translation.TranslationService;
import me.whereareiam.yui.common.localization.DiscordLocaleAdapter;
import me.whereareiam.yui.common.localization.YuiSemanticaLogger;
import me.whereareiam.yui.common.localization.format.LocaleFileHandler;
import me.whereareiam.yui.common.localization.format.MultiLocaleFileHandler;
import me.whereareiam.yui.common.localization.format.TemplateFileHandler;
import me.whereareiam.yui.common.localization.loader.DefaultFileTypeHandlerRegistry;
import me.whereareiam.yui.common.localization.loader.YuiTranslationLoader;
import me.whereareiam.yui.model.config.languages.Languages;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.config.languages.TranslationSettings;
import me.whereareiam.yui.localization.loader.FileTypeHandlerRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommonConfiguration {
	private final ObjectProvider<Settings> settingsProvider;
	private final ObjectProvider<Languages> languagesProvider;
	private JDA jda;

	@Bean
	@Primary
	public JDA jda() {
		Settings settings = settingsProvider.getObject();

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
	public void shutdownJda() {
		if (jda == null) return;

		log.info("Shutting down YUI");
		jda.shutdown();
	}

	@Bean(destroyMethod = "shutdown")
	public ScheduledExecutorService scheduledPool() {
		return Executors.newScheduledThreadPool(
				Math.max(2, Runtime.getRuntime().availableProcessors()),
				Thread.ofVirtual().name("yui-scheduled", 0).factory()
		);
	}

	@Bean
	public LocaleParser<DiscordLocale> localeParser() {
		return DiscordLocaleAdapter::wrap;
	}

	@Bean
	public FileTypeHandlerRegistry fileTypeHandlerRegistry() {
		FileTypeHandlerRegistry registry = new DefaultFileTypeHandlerRegistry();
		
		// Register built-in handlers
		registry.registerHandler(new LocaleFileHandler());
		registry.registerHandler(new MultiLocaleFileHandler());
		registry.registerHandler(new TemplateFileHandler());
		
		log.info("[Localization] Registered {} built-in file type handlers", registry.getHandlers().size());
		
		return registry;
	}

	@Bean
	public TranslationService<DiscordLocale> semanticaService(SemanticaConfiguration<DiscordLocale> config) {
		return Semantica.createService(config);
	}

	@Bean
	public SemanticaConfiguration<DiscordLocale> semanticaConfiguration(
			YuiTranslationLoader yuiTranslationLoader,
			LocaleParser<DiscordLocale> localeParser
	) {
		Settings settings = settingsProvider.getObject();
		TranslationSettings translationSettings = languagesProvider.getObject().getSettings();

		return SemanticaConfiguration.<DiscordLocale>builder()
				.defaultLocale(SemanticLocale.wrap(settings.getLocale().toLocale()))
				.performance(SemanticaConfiguration.PerformanceSettings.builder()
						.cache(SemanticaConfiguration.PerformanceSettings.CacheSettings.builder()
								.enabled(translationSettings.isCacheEnabled())
								.semiStaticSize(translationSettings.getCacheSemiStaticSize())
								.dynamicSize(translationSettings.getCacheDynamicSize())
								.semiStaticExpireMinutes(translationSettings.getCacheSemiStaticTtl())
								.dynamicExpireMinutes(translationSettings.getCacheDynamicTtl())
								.build())
						.prerenderStatic(translationSettings.isPrerenderStatic())
						.buildDependencyGraph(translationSettings.isBuildDependencyGraph())
						.logTimings(translationSettings.isLogTimings())
						.build())
				.localeParser(localeParser)
				.translationProvider(yuiTranslationLoader)
				.logger(new YuiSemanticaLogger())
				.build();
	}
}
