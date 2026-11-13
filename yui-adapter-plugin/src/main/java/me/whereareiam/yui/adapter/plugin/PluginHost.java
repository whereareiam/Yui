package me.whereareiam.yui.adapter.plugin;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.chassis.adapter.attache.AttacheLibraries;
import me.whereareiam.chassis.adapter.spring.SpringExtensionFactory;
import me.whereareiam.chassis.common.ChassisHost;
import me.whereareiam.chassis.common.HostOptions;
import me.whereareiam.chassis.common.loading.ExportLayer;
import me.whereareiam.chassis.exception.ExtensionException;
import me.whereareiam.chassis.model.InstalledExtension;
import me.whereareiam.chassis.model.Version;
import me.whereareiam.yui.api.BuildConfig;
import me.whereareiam.yui.api.model.update.UpdateConfiguration;
import me.whereareiam.yui.api.plugin.PluginManager;
import me.whereareiam.yui.api.plugin.YuiPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Yui's plugins through Chassis: the jars of {@code plugins/}, each with its descriptor {@code yui-plugin.yml}, its
 * libraries through Attache, its own class loader over Yui's export layer and a Spring context that is a child of
 * Yui's. Which plugins are off is kept in {@code plugins.yml}.
 */
@Slf4j
@Component
public class PluginHost implements PluginManager {
	// What plugins see of Yui: its API and the libraries it hands them.
	private static final List<String> EXPORTS = List.of(
			"me.whereareiam.yui.api", "me.whereareiam.chassis", "me.whereareiam.conditia", "me.whereareiam.semantica",
			"org.springframework", "net.dv8tion.jda", "org.jetbrains.annotations"
	);

	private final ChassisHost<ExtensionContext, Fields> host;

	public PluginHost(ApplicationContext yui, @Qualifier("pluginsPath") Path plugins, @Qualifier("dataPath") Path data) {
		HostOptions options = HostOptions.builder()
				.kind("plugin")
				.folder(plugins)
				.descriptorFile("yui-plugin.yml")
				.dataFolder(plugins)
				.selectionFile(data.resolve("plugins.yml"))
				.hostVersion(version())
				.platform(new YuiPlatform())
				.exportLayer(new ExportLayer(PluginHost.class.getClassLoader(), EXPORTS))
				.preparation(AttacheLibraries.builder()
						.folder(data.resolve("libraries"))
						.repository("https://registry.whereareiam.me/maven/packages")
						.build())
				.build();
		this.host = new ChassisHost<>(options, YuiPlugin.class, Fields.class, (base, fields) -> base, new SpringExtensionFactory(yui));
	}

	/**
	 * Starts every plugin that can start, dependencies first.
	 */
	public void start() {
		try {
			host.start();
		} catch (IOException exception) {
			throw new UncheckedIOException("The plugins' folder or plugins.yml cannot be read", exception);
		}
	}

	@PreDestroy
	void stop() {
		host.stop();
	}

	@Override
	public @NotNull List<InstalledExtension> plugins() {
		return host.list();
	}

	@Override
	public @NotNull Optional<InstalledExtension> find(@NotNull String id) {
		return host.find(id);
	}

	@Override
	public @NotNull InstalledExtension enable(@NotNull String id) throws ExtensionException {
		return host.enable(id);
	}

	@Override
	public @NotNull InstalledExtension disable(@NotNull String id) throws ExtensionException {
		return host.disable(id);
	}

	@Override
	public @NotNull InstalledExtension reload(@NotNull String id) throws ExtensionException {
		return host.reload(id);
	}

	@Override
	public @NotNull List<InstalledExtension> rescan() {
		return host.rescan();
	}

	@Override
	public @NotNull Optional<UpdateConfiguration> updater(@NotNull String id) {
		return host.fields(id).map(Fields::updater);
	}

	// A development build has no version; it is newer than every release a plugin may name.
	private static Version version() {
		try {
			return Version.parse(BuildConfig.VERSION);
		} catch (IllegalArgumentException exception) {
			return Version.parse("999.0.0");
		}
	}

	/**
	 * Yui's own fields in a plugin's descriptor.
	 *
	 * @param updater where the plugin's updates come from
	 */
	public record Fields(@Nullable UpdateConfiguration updater) {
	}
}
