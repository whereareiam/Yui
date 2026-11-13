package me.whereareiam.yui.common.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.semantica.catalog.Layer;
import me.whereareiam.semantica.common.Semantica;
import me.whereareiam.semantica.source.file.FileSource;
import me.whereareiam.yui.message.MessageDefaults;
import me.whereareiam.yui.message.MessageLayers;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * Adds an owner's texts to Yui's Semantica as layers it owns, with its id as key prefix.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticaMessageLayers implements MessageLayers {
	private final Semantica semantica;

	@Override
	public void add(@NotNull String owner, @NotNull List<? extends MessageDefaults<?>> defaults, @NotNull Path folder) {
		semantica.replace(owner,
				Layer.owned(owner, new DefaultsSource(owner + ":defaults", owner, defaults, MessageConfiguration.TEXTS.mapper()), Layer.DEFAULTS),
				Layer.owned(owner, FileSource.builder(folder, MessageConfiguration.TEXTS).prefix(owner).build(), Layer.OVERRIDES));
		log.info("[Messages] Loaded the texts of {} ({} default files)", owner, defaults.size());
	}

	@Override
	public void remove(@NotNull String owner) {
		semantica.remove(owner);
	}
}
