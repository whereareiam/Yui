package me.whereareiam.yui.adapter.command;

import me.whereareiam.yui.DefinitionProvider;
import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.adapter.command.definition.DefinitionProviderRegistry;
import me.whereareiam.yui.adapter.command.registration.CommandDefinitionParser;
import me.whereareiam.yui.adapter.command.registration.annotation.AnnotationCommandRegistrar;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.type.CommandCategory;
import me.whereareiam.yui.type.Source;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CommandRegistrationLifecycleTest {
	private static final class TestCommandManager extends CommandManager<Object> {
		TestCommandManager() {
			super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
		}

		@Override
		public boolean hasPermission(@NotNull Object sender, @NotNull String permission) {
			return true;
		}
	}

	private static final class TestDefinitionProvider implements DefinitionProvider {
		private final String id;
		private final Source source;
		private final Map<String, CommandDefinition> defs;

		TestDefinitionProvider(String id, Source source, Map<String, CommandDefinition> defs) {
			this.id = id;
			this.source = source;
			this.defs = defs;
		}

		@Override
		public @NotNull String id() {
			return id;
		}

		@Override
		public @NotNull Source source() {
			return source;
		}

		@Override
		public @NotNull Map<String, CommandDefinition> definitions() {
			return defs;
		}
	}

	// Commands
	private static final class InternalCmd {
		@Definition("internal-cmd")
		@Command("internal")
		public void cmd(CommandContext<Object> ctx) {}
	}

	private static final class PluginCmdEnabled {
		@Definition("plugin-enabled")
		@Command("plugA")
		public void cmd(CommandContext<Object> ctx) {}
	}

	private static final class PluginCmdDisabled {
		@Definition("plugin-disabled")
		@Command("plugB")
		public void cmd(CommandContext<Object> ctx) {}
	}

	private static final class PluginCmdUpdated {
		@Definition("plugin-updated")
		@Command("plugC")
		public void cmd(CommandContext<Object> ctx) {}
	}

	@Test
	void providersMergeAndReloadLifecycle() {
		// initial internal provider
		CommandDefinition internalDef = def(true, List.of("internal"), "Internal v1");
		TestDefinitionProvider internalProvider = new TestDefinitionProvider("internal", Source.INTERNAL, Map.of("internal-cmd", internalDef));

		DefinitionProviderRegistry registry = simpleProvider(internalProvider);
		CommandDefinitionRegistry defRegistry = new CommandDefinitionRegistry();

		// initial registration (internal only)
		var manager = registerAll(registry, defRegistry, List.of(new InternalCmd()));
		assertEquals(1, manager.commands().size());
		assertTrue(defRegistry.get("internal-cmd").isPresent(), "Internal def present");
		assertDescription(manager, "internal", "Internal v1");

		// add plugin provider (one enabled, one disabled)
		CommandDefinition pluginEnabledV1 = def(true, List.of("plugA"), "Plugin v1");
		CommandDefinition pluginDisabled = def(false, List.of("plugB"), "Disabled");
		TestDefinitionProvider pluginProviderV1 = new TestDefinitionProvider("plugin:demo", Source.EXTERNAL,
				Map.of("plugin-enabled", pluginEnabledV1, "plugin-disabled", pluginDisabled));
		registry.addExternalProvider(pluginProviderV1);

		manager = registerAll(registry, defRegistry, List.of(new InternalCmd(), new PluginCmdEnabled(), new PluginCmdDisabled()));
		assertEquals(2, manager.commands().size(), "Internal + enabled plugin");
		assertTrue(defRegistry.get("plugin-enabled").isPresent());
		assertTrue(defRegistry.get("plugin-disabled").isPresent());
		assertTrue(defRegistry.get("internal-cmd").isPresent());
		assertDescription(manager, "plugA", "Plugin v1");
		assertDescription(manager, "internal", "Internal v1");

		// update plugin provider with changed description
		CommandDefinition pluginEnabledV2 = def(true, List.of("plugA"), "Plugin v2");
		TestDefinitionProvider pluginProviderV2 = new TestDefinitionProvider("plugin:demo", Source.EXTERNAL,
				Map.of("plugin-enabled", pluginEnabledV2, "plugin-disabled", pluginDisabled));
		registry.addExternalProvider(pluginProviderV2);

		manager = registerAll(registry, defRegistry, List.of(new InternalCmd(), new PluginCmdEnabled(), new PluginCmdDisabled()));
		assertEquals(2, manager.commands().size(), "Internal + updated plugin");
		assertDescription(manager, "plugA", "Plugin v2");

		// remove plugin provider (simulate unload)
		registry.removeExternalProvider("plugin:demo");
		defRegistry.removeBySource("plugin:demo", Source.EXTERNAL);
		manager = registerAll(registry, defRegistry, List.of(new InternalCmd()));
		assertEquals(1, manager.commands().size(), "Only internal after plugin unload");
		assertFalse(defRegistry.get("plugin-enabled").isPresent());

		// reload plugin with updated defs: disable previous, enable new command
		CommandDefinition pluginUpdated = def(true, List.of("plugC"), "Plugin v3");
		TestDefinitionProvider pluginProviderV3 = new TestDefinitionProvider("plugin:demo", Source.EXTERNAL,
				Map.of("plugin-enabled", def(false, List.of("plugA"), "Plugin disabled"),
						"plugin-updated", pluginUpdated));
		registry.addExternalProvider(pluginProviderV3);
		manager = registerAll(registry, defRegistry, List.of(new InternalCmd(), new PluginCmdEnabled(), new PluginCmdUpdated()));

		assertEquals(2, manager.commands().size(), "Internal + updated plugin command only");
		assertFalse(manager.commands().stream().anyMatch(c -> c.rootComponent().name().equals("plugA")), "Disabled command should not be registered");
		assertTrue(manager.commands().stream().anyMatch(c -> c.rootComponent().name().equals("plugC")));
		assertDescription(manager, "plugC", "Plugin v3");
	}

	private CommandDefinition def(boolean enabled, List<String> aliases, String description) {
		return new CommandDefinition(
				enabled,
				aliases,
				description,
				null,
				"{alias}",
				Map.of(),
				CommandCategory.NONE,
				new me.whereareiam.yui.model.command.CommandCooldown(false, 0, ""),
				null
		);
	}

	private DefinitionProviderRegistry simpleProvider(DefinitionProvider... providers) {
		return new DefinitionProviderRegistry(new ObjectProvider<>() {
			@NotNull
			@Override
			public DefinitionProvider getObject(@NotNull Object... args) {
				throw new UnsupportedOperationException();
			}

			@Override
			public DefinitionProvider getIfAvailable() {
				return providers.length > 0 ? providers[0] : null;
			}

			@Override
			public DefinitionProvider getIfUnique() {
				return getIfAvailable();
			}

			@NotNull
			@Override
			public DefinitionProvider getObject() {
				return getIfAvailable();
			}

			@NotNull
			@Override
			public java.util.stream.Stream<DefinitionProvider> stream() {
				return java.util.Arrays.stream(providers);
			}

			@NotNull
			@Override
			public java.util.stream.Stream<DefinitionProvider> orderedStream() {
				return stream();
			}
		});
	}

	private TestCommandManager registerAll(
			DefinitionProviderRegistry providerRegistry,
			CommandDefinitionRegistry defRegistry,
			List<Object> containers
	) {
		TestCommandManager manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		Map<String, DefinitionProviderRegistry.ProviderEntry> merged = providerRegistry.merged();
		Map<String, CommandDefinition> defs = merged.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().definition()));

		AnnotationCommandRegistrar<Object> registrar = new AnnotationCommandRegistrar<>(parser, Object.class, defs::get);
		registrar.register(null, containers.toArray());

		// persist to registry
		merged.forEach((id, entry) -> defRegistry.put(id, entry.id(), entry.source(), entry.definition()));

		return manager;
	}

	private void assertDescription(TestCommandManager manager, String rootAlias, String expected) {
		var command = manager.commands().stream()
				.filter(c -> c.rootComponent().name().equals(rootAlias))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Command " + rootAlias + " not registered"));

		String description = command.commandDescription().description().textDescription();
		assertEquals(expected, description, "Description for " + rootAlias);
	}
}
