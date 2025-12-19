package me.whereareiam.yui.adapter.command.registration;

import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.adapter.command.parsing.definition.CommandDefinitionParser;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.model.command.CommandCooldown;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.type.CommandCategory;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.key.CloudKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationCommandRegistrarIntegrationTest {
	private static final class TestCommandManager extends CommandManager<Object> {
		TestCommandManager() {
			super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
		}

		@Override
		public boolean hasPermission(@NotNull Object sender, @NotNull String permission) {
			return true;
		}
	}

	@Test
	void registersWhenNoDefinitionId() {
		class NoDefinitionCmd {
			@Command("ping")
			public void cmd(CommandContext<Object> ctx) {}
		}

		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		AnnotationCommandRegistrar<Object> registrar = new AnnotationCommandRegistrar<>(parser, Object.class, _ -> null);

		registrar.register(null, new NoDefinitionCmd());

		Collection<org.incendo.cloud.Command<Object>> commands = manager.commands();
		assertEquals(1, commands.size(), "Command should be registered even without definition id");
		CloudKey<String> key = YuiCommandMetaKeys.DEFINITION;
		assertFalse(commands.iterator().next().commandMeta().optional(key).isPresent(), "Definition meta should be absent");
	}

	@Test
	void registersWhenDefinitionMissing() {
		class MissingDefCmd {
			@Definition("missing")
			@Command("hello")
			public void cmd(CommandContext<Object> ctx) {}
		}

		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		AnnotationCommandRegistrar<Object> registrar = new AnnotationCommandRegistrar<>(parser, Object.class, _ -> null);

		registrar.register(null, new MissingDefCmd());

		Collection<org.incendo.cloud.Command<Object>> commands = manager.commands();
		assertEquals(1, commands.size());
		CloudKey<String> key = YuiCommandMetaKeys.DEFINITION;
		assertEquals("missing", commands.iterator().next().commandMeta().optional(key).orElse(null),
				"Definition meta should reflect @Definition even if lookup is missing");
	}

	@Test
	void skipsWhenDefinitionDisabled() {
		class DisabledCmd {
			@Definition("off")
			@Command("offcmd")
			public void cmd(CommandContext<Object> ctx) {}
		}

		CommandDefinition disabled = new CommandDefinition(
				false,
				java.util.List.of("offcmd"),
				null,
				null,
				"",
				Map.of(),
				CommandCategory.NONE,
				new CommandCooldown(false, 0, ""),
				null
		);

		Map<String, CommandDefinition> defs = new HashMap<>();
		defs.put("off", disabled);

		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		AnnotationCommandRegistrar<Object> registrar = new AnnotationCommandRegistrar<>(parser, Object.class, defs::get);

		registrar.register(null, new DisabledCmd());

		assertEquals(0, manager.commands().size(), "Disabled definition should prevent registration");
	}

	@Test
	void registersWithRootWhenEnabledAndUsageContainsCommand() {
		class ReloadCmd {
			@Definition("reload-def")
			@Command("reload")
			public void cmd(CommandContext<Object> ctx) {}
		}

		CommandDefinition enabled = new CommandDefinition(
				true,
				java.util.List.of("reload"),
				null,
				null,
				"{command} reload",
				Map.of(),
				CommandCategory.NONE,
				new CommandCooldown(false, 0, ""),
				null
		);

		Map<String, CommandDefinition> defs = Map.of("reload-def", enabled);

		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		AnnotationCommandRegistrar<Object> registrar = new AnnotationCommandRegistrar<>(parser, Object.class, defs::get);

		registrar.register("yui", new ReloadCmd());

		Collection<org.incendo.cloud.Command<Object>> commands = manager.commands();
		assertEquals(1, commands.size(), "Should register enabled command");
		org.incendo.cloud.Command<Object> cmd = commands.iterator().next();
		assertEquals("yui", cmd.rootComponent().name(), "Root should be set from provided root");
		assertTrue(cmd.components().stream().anyMatch(c ->
				c.type() == CommandComponent.ComponentType.LITERAL && c.name().equals("reload")),
				"Reload literal should be present as subcommand under root");
	}
}

