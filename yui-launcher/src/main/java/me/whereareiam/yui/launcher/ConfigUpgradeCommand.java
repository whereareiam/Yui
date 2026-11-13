package me.whereareiam.yui.launcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.strata.model.MigrationReport;
import me.whereareiam.yui.common.config.ConfiguraConfiguration;
import me.whereareiam.yui.common.config.resolver.FileSystemConfigurationTypeResolver;
import me.whereareiam.yui.common.config.upgrade.ConfigUpgrade;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * {@code yui config upgrade <dir>}: brings the configuration files of a folder up to this version's shape, without
 * starting Yui. Run it on the repository or folder that holds them.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ConfigUpgradeCommand {
	static boolean matches(String[] args) {
		return args.length >= 2 && args[0].equals("config") && args[1].equals("upgrade");
	}

	static int run(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: yui config upgrade <dir>");
			return 2;
		}

		Path folder = Path.of(args[2]).toAbsolutePath().normalize();
		if (!Files.isDirectory(folder)) {
			System.err.println(folder + " is not a folder");
			return 2;
		}

		ConfigUpgrade upgrade = new ConfigUpgrade(
				ConfiguraConfiguration.build(new FileSystemConfigurationTypeResolver(folder).getConfigurationType()),
				folder
		);
		List<String> pending = upgrade.pending();
		if (pending.isEmpty()) {
			System.out.println(folder + " is up to date");
			return 0;
		}

		MigrationReport report = upgrade.run();
		report.getApplied().forEach((stream, applied) -> applied.forEach(migration ->
				System.out.println("Applied " + stream + " " + migration)));

		return 0;
	}
}
