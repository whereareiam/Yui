package me.whereareiam.yui.common.config.upgrade;

import me.whereareiam.configura.Configura;
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
 * Brings a folder of Yui's configuration files up to the shape this version reads, through Strata. Its version is
 * kept as {@code _version} in {@code settings.yml}.
 *
 * <p>Yui never writes its files, so an upgrade is run on the folder that holds them, with
 * {@code yui config upgrade <dir>}; Yui refuses to start on files that still need one.</p>
 */
public final class ConfigUpgrade {
	private static final String STREAM = "yui/config";
	private static final String VERSION_FILE = "settings.yml";

	private final Strata strata;

	public ConfigUpgrade(@NotNull Configura configura, @NotNull Path folder) {
		MigrationStream<ConfigContext> stream = MigrationStream.<ConfigContext>builder()
				.id(STREAM)
				.target(new ConfiguraTarget(configura, folder, VERSION_FILE))
				.baseline((files, latest) -> files.exists(VERSION_FILE) ? OptionalInt.of(0) : OptionalInt.empty())
				.migration(1, "settings-and-persistence", SettingsMigration::apply)
				.build();
		this.strata = new Strata(List.of(stream));
	}

	/**
	 * @return the names of the migrations the folder still needs, empty when it is up to date or new
	 */
	public @NotNull List<String> pending() {
		return strata.pending().values().stream()
				.flatMap(List::stream)
				.map(migration -> migration.getVersion() + " " + migration.getName())
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
