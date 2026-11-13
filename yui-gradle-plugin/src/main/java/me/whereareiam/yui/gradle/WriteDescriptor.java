package me.whereareiam.yui.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes {@code yui-plugin.yml}, the descriptor Yui reads from a plugin's jar.
 */
public abstract class WriteDescriptor extends DefaultTask {
	@Input
	public abstract Property<String> getId();

	@Input
	public abstract Property<String> getVersion();

	@Input
	public abstract Property<String> getMain();

	@Input
	public abstract ListProperty<String> getDependencies();

	@Input
	public abstract ListProperty<String> getExports();

	@Input
	@Optional
	public abstract Property<String> getUpdates();

	@OutputDirectory
	public abstract DirectoryProperty getOutput();

	@TaskAction
	public void write() throws IOException {
		if (!getId().isPresent() || !getMain().isPresent())
			throw new GradleException("yuiPlugin { } needs an id and a main class");

		StringBuilder yaml = new StringBuilder()
				.append("id: ").append(getId().get()).append('\n')
				.append("version: ").append(quoted(semantic(getVersion().get()))).append('\n')
				.append("main: ").append(getMain().get()).append('\n');
		list(yaml, "dependencies", getDependencies().get().stream().map(id -> "id: " + id).toList());
		list(yaml, "exports", getExports().get());
		updates(yaml);

		Path file = getOutput().get().getAsFile().toPath().resolve("yui-plugin.yml");
		Files.createDirectories(file.getParent());
		Files.writeString(file, yaml);
	}

	private void updates(StringBuilder yaml) {
		if (!getUpdates().isPresent() || getUpdates().get().isBlank()) return;

		String[] where = getUpdates().get().split(":", 2);
		if (where.length != 2) throw new GradleException("yuiPlugin.updates is <provider>:<id>, such as GITHUB:whereareiam/Yui");

		yaml.append("updater:\n  release: { provider: ").append(where[0]).append(", id: ").append(where[1]).append(" }\n");
	}

	private static void list(StringBuilder yaml, String key, List<String> entries) {
		if (entries.isEmpty()) return;

		yaml.append(key).append(":\n");
		entries.forEach(entry -> yaml.append("  - ").append(entry).append('\n'));
	}

	// Chassis reads semantic versions only: a build's version that is none, such as dev or dev-1a2b3c4, becomes a
	// pre-release below every release (0.0.0-dev).
	private static String semantic(String version) {
		return version.matches("\\d+(\\.\\d+){0,2}([-+].*)?") ? version : "0.0.0-" + version;
	}

	private static String quoted(String text) {
		return "\"" + text.replace("\"", "\\\"") + "\"";
	}
}
