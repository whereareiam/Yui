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
import java.util.Map;
import java.util.OptionalInt;

/**
 * Brings a folder of Yui's configuration files up to the shape this version reads, through Strata: one stream per
 * file, whose version the file keeps as {@code _version}.
 *
 * <p>Yui never writes its files, so an upgrade is run on the folder that holds them, with
 * {@code yui config upgrade <dir>}; Yui refuses to start on files that still need one.</p>
 */
public final class ConfigUpgrade {
	// A migration's name never changes once released: an installation that ran it only remembers its version.
	private static final Map<String, List<String>> NAMES = Map.of(
			"settings", List.of("agreed-shape", "audit-into-its-file", "conversations-into-their-file"),
			"languages", List.of("agreed-shape"),
			"style/palette", List.of("agreed-shape"),
			"style/embeds", List.of("agreed-shape")
	);

	private final Strata strata;

	public ConfigUpgrade(@NotNull Configura configura, @NotNull Path folder) {
		this.strata = new Strata(List.of(
				stream(configura, folder, "settings", "settings.yml", "settings.yml", SettingsMigration::apply, AuditMigration::apply, ConversationsMigration::apply),
				stream(configura, folder, "languages", "languages.yml", "languages.yml", LanguagesMigration::apply),
				stream(configura, folder, "style/palette", "style/palette.yml", StyleMigration.OLD_PALETTE, StyleMigration::palette),
				stream(configura, folder, "style/embeds", "style/embeds.yml", StyleMigration.OLD_EMBEDS, StyleMigration::embeds)
		));
	}

	// One stream per file, so each file carries its own _version. A file without one may still be old, unless neither
	// it nor its old place exists: then the installation is new. Every migration does nothing to a file already in its
	// new shape.
	@SafeVarargs
	private static MigrationStream<ConfigContext> stream(
			Configura configura,
			Path folder,
			String name,
			String file,
			String oldFile,
			MigrationAction<ConfigContext>... migrations
	) {
		MigrationStream.Builder<ConfigContext> stream = MigrationStream.<ConfigContext>builder()
				.id("yui/config/" + name)
				.target(new ConfiguraTarget(configura, folder, file))
				.baseline((files, latest) -> files.exists(file) || files.exists(oldFile) ? OptionalInt.of(0) : OptionalInt.empty());
		for (int version = 1; version <= migrations.length; version++)
			stream.migration(version, NAMES.get(name).get(version - 1), migrations[version - 1]);

		return stream.build();
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
