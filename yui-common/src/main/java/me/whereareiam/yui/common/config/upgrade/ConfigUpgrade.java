package me.whereareiam.yui.common.config.upgrade;

import me.whereareiam.configura.Configura;
import me.whereareiam.strata.MigrationAction;
import me.whereareiam.strata.MigrationStream;
import me.whereareiam.strata.Strata;
import me.whereareiam.strata.adapter.configura.ConfigContext;
import me.whereareiam.strata.adapter.configura.ConfiguraTarget;
import me.whereareiam.strata.model.MigrationReport;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;

/**
 * Brings a folder of Yui's configuration files up to the shape this version reads, through Strata: one stream per
 * file, whose version the file keeps as {@code _version}.
 *
 * <p>Yui never writes its files, so an upgrade is run on the folder that holds them, with
 * {@code yui config upgrade <dir>}; Yui refuses to start on files that still need one.</p>
 */
public final class ConfigUpgrade {
	private final Strata strata;

	public ConfigUpgrade(@NotNull Configura configura, @NotNull Path folder) {
		this.strata = new Strata(List.of(
				stream(configura, folder, "settings", "settings.yml", "settings.yml", SettingsMigration::apply),
				stream(configura, folder, "languages", "languages.yml", "languages.yml", LanguagesMigration::apply),
				stream(configura, folder, "style/palette", "style/palette.yml", StyleMigration.OLD_PALETTE, StyleMigration::palette),
				stream(configura, folder, "style/embeds", "style/embeds.yml", StyleMigration.OLD_EMBEDS, StyleMigration::embeds)
		));
	}

	// One stream per file, so each file carries its own _version. A file without one may still be old, unless neither
	// it nor its old place exists: then the installation is new. Every migration does nothing to a file already in its
	// new shape.
	private static MigrationStream<ConfigContext> stream(
			Configura configura,
			Path folder,
			String name,
			String file,
			String oldFile,
			MigrationAction<ConfigContext> first
	) {
		return MigrationStream.<ConfigContext>builder()
				.id("yui/config/" + name)
				.target(new ConfiguraTarget(configura, folder, file))
				.baseline((files, latest) -> files.exists(file) || files.exists(oldFile) ? OptionalInt.of(0) : OptionalInt.empty())
				.migration(1, "agreed-shape", first)
				.build();
	}

	/**
	 * @return the names of the migrations the folder still needs, empty when it is up to date or new
	 */
	public @NotNull List<String> pending() {
		return strata.pending().entrySet().stream()
				.flatMap(stream -> stream.getValue().stream()
						.map(migration -> stream.getKey() + " " + migration.getVersion() + " " + migration.getName()))
				.toList();
	}

	/**
	 * Runs the migrations the folder still needs and rewrites the files they change.
	 *
	 * @return what was run
	 */
	public @NotNull MigrationReport run() {
		return strata.migrate();
	}
}
