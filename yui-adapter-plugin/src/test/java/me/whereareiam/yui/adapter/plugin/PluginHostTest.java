package me.whereareiam.yui.adapter.plugin;

import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.chassis.testing.ExtensionJar;
import me.whereareiam.chassis.testing.LeakCheck;
import me.whereareiam.chassis.type.ExtensionState;
import me.whereareiam.yui.api.plugin.PluginRegistrations;
import me.whereareiam.yui.api.plugin.YuiPlugin;
import me.whereareiam.yui.api.service.InteractionService;
import org.example.greeting.Greeter;
import org.example.greeting.GreetingPlugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Yui's plugin runtime on a real Chassis host: a plugin jar with {@code @Plugin} starts in a Spring context of its own,
 * is attached to Yui while it starts, detached when it stops, and leaves no class loader behind. It sees Yui's API
 * and none of Yui's implementation.
 */
class PluginHostTest {
	@TempDir
	Path folder;

	private GenericApplicationContext yui;
	private Recording registrations;
	private PluginHost host;

	@BeforeEach
	void setUp() throws Exception {
		ExtensionJar.of("yui-plugin.yml", """
				id: greeting
				version: 1.0.0
				main: org.example.greeting.GreetingPlugin
				updater:
				  release: { provider: GITHUB, id: example/greeting }
				""").with(GreetingPlugin.class, Greeter.class).write(folder.resolve("plugins/greeting.jar"));

		registrations = new Recording();
		yui = new GenericApplicationContext();
		yui.registerBean(PluginRegistrations.class, () -> registrations);
		yui.registerBean(InteractionService.class, () -> mock(InteractionService.class));
		yui.refresh();

		host = new PluginHost(yui, folder.resolve("plugins"), folder);
	}

	@AfterEach
	void tearDown() {
		host.stop();
		yui.close();
	}

	@Test
	void aPlugin_StartsInItsOwnContextAndIsAttached() {
		host.start();

		assertEquals(ExtensionState.ENABLED, host.find("greeting").orElseThrow().getState());
		assertEquals(List.of("attach greeting"), registrations.events);
		assertNotSame(Greeter.class.getClassLoader(), registrations.loader, "The plugin's beans come from its own jar");
		assertTrue(host.updater("greeting").isPresent());
	}

	@Test
	void aDisabledPlugin_IsDetachedAndCollected() throws Exception {
		host.start();
		WeakReference<ClassLoader> loader = new WeakReference<>(registrations.loader);
		registrations.loader = null;

		host.disable("greeting");

		assertEquals(ExtensionState.DISABLED, host.find("greeting").orElseThrow().getState());
		assertEquals(List.of("attach greeting", "detach greeting"), registrations.events);
		assertTrue(LeakCheck.collected(loader, Duration.ofSeconds(10)), "The plugin's class loader is still reachable");
	}

	@Test
	void aPlugin_SeesYuisApiButNotItsImplementation() throws Exception {
		host.start();
		ClassLoader plugin = registrations.loader;

		assertEquals(YuiPlugin.class, plugin.loadClass(YuiPlugin.class.getName()));
		assertThrows(ClassNotFoundException.class, () -> plugin.loadClass(PluginHost.class.getName()));
	}

	// Records what Yui is told, as DefaultPluginRegistrations would act on it.
	private static final class Recording implements PluginRegistrations {
		private final List<String> events = new ArrayList<>();
		private ClassLoader loader;

		@Override
		public @NotNull AutoCloseable attach(@NotNull ApplicationContext plugin, @NotNull ExtensionContext extension) {
			String id = extension.descriptor().getId();
			events.add("attach " + id);
			loader = plugin.getBean("greeter").getClass().getClassLoader();

			return () -> events.add("detach " + id);
		}
	}
}
