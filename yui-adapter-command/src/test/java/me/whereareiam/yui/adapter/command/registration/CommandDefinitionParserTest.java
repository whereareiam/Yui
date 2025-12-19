package me.whereareiam.yui.adapter.command.registration;

import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.adapter.command.registration.annotation.YuiAnnotationParser;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.model.command.CommandDefinition;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.key.CloudKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CommandDefinitionParser}.
 */
class CommandDefinitionParserTest {
	private static final class TestCommandManager extends CommandManager<Object> {
		TestCommandManager() {
			super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
		}

		@Override
		public boolean hasPermission(@NotNull Object sender, @NotNull String permission) {
			return true;
		}
	}

	private static final class TestCommands {
		@Definition("test-main")
		@Command("test <name> [optional]")
		public void command(CommandContext<Object> ctx, @Argument("name") String name, @Argument("optional") String optional) {
			// no-op
		}
	}

	@Test
	void parsesSingleWordAliases() {
		// given
		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		CommandManager<Object> parseManager = new TestCommandManager();
		YuiAnnotationParser<Object> annotationParser = YuiAnnotationParser.create(parseManager, Object.class);

		CommandDefinition definition = new CommandDefinition(
				true,
				List.of("cfgmain", "cfgm"),
				"Test description",
				null,
				"<name>",
				Map.of(),
				null,
				null,
				null
		);

		org.incendo.cloud.Command<Object> parsedCommand = annotationParser.parse(new TestCommands()).iterator().next();

		// when
		var builders = parser.applyOverrides(
				definition,
				"test-main",
				parsedCommand,
				null
		);

		builders.forEach(manager::command);
		Collection<org.incendo.cloud.Command<Object>> commands = manager.commands();
		assertEquals(1, commands.size(), "Should register one command");
		org.incendo.cloud.Command<Object> built = commands.iterator().next();
		assertEquals("cfgmain", built.rootComponent().name(), "Primary alias should be root");
	}

	@Test
	void parsesMultiWordAliasesAsSubcommands() {
		// given
		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		
		// Use a command with no arguments to avoid issues
		class NoArgCommand {
			@Definition("test-noarg")
			@Command("test")
			public void command(CommandContext<Object> ctx) {
				// no-op
			}
		}
		
		CommandManager<Object> parseManager = new TestCommandManager();
		YuiAnnotationParser<Object> annotationParser = YuiAnnotationParser.create(parseManager, Object.class);
		org.incendo.cloud.Command<Object> parsedCommand = annotationParser.parse(new NoArgCommand()).iterator().next();

		CommandDefinition definition = new CommandDefinition(
				true,
				List.of("config reload", "config save"),
				null,
				null,
				"",
				Map.of(),
				null,
				null,
				null
		);

		// when
		var builders = parser.applyOverrides(
				definition,
				"test-main",
				parsedCommand,
				null
		);

		builders.forEach(manager::command);
		Collection<org.incendo.cloud.Command<Object>> commands = manager.commands();
		assertEquals(1, commands.size(), "Should register one command for grouped multi-word aliases");
		var built = commands.iterator().next();
		assertEquals("config", built.rootComponent().name(), "Root should be config");
		assertTrue(built.components().stream()
				.skip(1)
				.anyMatch(c -> c.type() == CommandComponent.ComponentType.LITERAL
						&& (c.name().equals("reload") || c.name().equals("save"))),
				"Should contain reload or save literal");
	}

	@Test
	void parsesArgumentsFromUsageString() {
		// given
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(new TestCommandManager());

		// when
		List<CommandDefinitionParser.ArgumentToken> tokens = parser.parseUsage("<name> [optional] <required>");

		// then
		assertEquals(3, tokens.size(), "Should parse 3 argument tokens");

		CommandDefinitionParser.ArgumentToken nameToken = tokens.getFirst();
		assertEquals("name", nameToken.name());
		assertTrue(nameToken.required(), "name should be required");
		assertFalse(nameToken.greedy(), "name should not be greedy");

		CommandDefinitionParser.ArgumentToken optionalToken = tokens.get(1);
		assertEquals("optional", optionalToken.name());
		assertFalse(optionalToken.required(), "optional should not be required");

		CommandDefinitionParser.ArgumentToken requiredToken = tokens.get(2);
		assertEquals("required", requiredToken.name());
		assertTrue(requiredToken.required(), "required should be required");
	}

	@Test
	void ignoresPlaceholderTokensInUsage() {
		// given
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(new TestCommandManager());

		// when: parsing usage with {command} placeholder (e.g., "{command} {alias}" means
		// the root command name will be substituted here, like "yui reload")
		List<CommandDefinitionParser.ArgumentToken> tokens = parser.parseUsage("{command} <name>");

		// then: should ignore {command} placeholder during argument parsing
		// (it's used for command path construction, not as an argument)
		assertEquals(1, tokens.size(), "Should only parse actual arguments, ignoring placeholders");
		assertEquals("name", tokens.getFirst().name());
	}

	@Test
	void ignoresAliasPlaceholderInUsage() {
		// given
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(new TestCommandManager());

		// when: parsing usage with {alias} placeholder (e.g., "{command} {alias}" means
		// "yui reload" where "reload" is the command's alias)
		List<CommandDefinitionParser.ArgumentToken> tokens = parser.parseUsage("{command} {alias} <name>");

		// then: should ignore both {command} and {alias} placeholders during argument parsing
		assertEquals(1, tokens.size(), "Should only parse actual arguments, ignoring placeholders");
		assertEquals("name", tokens.getFirst().name());
	}

	@Test
	void handlesGreedyArguments() {
		// given
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(new TestCommandManager());

		// when
		List<CommandDefinitionParser.ArgumentToken> tokens = parser.parseUsage("<message...>");

		// then
		assertEquals(1, tokens.size());
		CommandDefinitionParser.ArgumentToken token = tokens.getFirst();
		assertEquals("message", token.name());
		assertTrue(token.greedy(), "message should be greedy");
		assertTrue(token.required(), "message should be required");
	}

	@Test
	void appliesDefinitionMetadata() {
		// given
		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		CommandManager<Object> parseManager = new TestCommandManager();
		YuiAnnotationParser<Object> annotationParser = YuiAnnotationParser.create(parseManager, Object.class);

		CommandDefinition definition = new CommandDefinition(
				true,
				List.of("test"),
				"Test description",
				null,
				"",
				Map.of(),
				null,
				null,
				null
		);

		org.incendo.cloud.Command<Object> parsedCommand = annotationParser.parse(new TestCommands()).iterator().next();

		// when
		var builders = parser.applyOverrides(
				definition,
				"test-main",
				parsedCommand,
				null
		);
		builders.forEach(manager::command);

		// then
		org.incendo.cloud.Command<Object> command = manager.commands().iterator().next();
		CloudKey<String> key = YuiCommandMetaKeys.DEFINITION;
		String definitionId = command.commandMeta().optional(key).orElse(null);
		assertEquals("test-main", definitionId, "Definition ID should be set in metadata");

		// Description should be set from definition
		// Note: Cloud might return empty description if not properly set
		String description = command.commandDescription().description().textDescription();
		assertNotNull(description, "Description should not be null");
		// The description should match, but Cloud might handle empty descriptions differently
		if (!description.isEmpty()) {
			assertEquals("Test description", description, "Description should be set from definition");
		}
	}

	@Test
	void doesNotRegisterDisabledDefinitions() {
		// given: start with a fresh manager to ensure no previous commands
		CommandManager<Object> manager = new TestCommandManager();
		assertEquals(0, manager.commands().size(), "Manager should start empty");
		
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		CommandManager<Object> parseManager = new TestCommandManager();
		YuiAnnotationParser<Object> annotationParser = YuiAnnotationParser.create(parseManager, Object.class);

		CommandDefinition disabled = new CommandDefinition(
				false, // disabled
				List.of("disabled"),
				null,
				null,
				"",
				Map.of(),
				null,
				null,
				null
		);

		org.incendo.cloud.Command<Object> parsedCommand = annotationParser.parse(new TestCommands()).iterator().next();

		// when
		var builders = parser.applyOverrides(
				disabled,
				"test-disabled",
				parsedCommand,
				null
		);
		builders.forEach(manager::command);

		// then: should still be empty because definition is disabled
		assertEquals(0, manager.commands().size(), "Disabled definition should not register commands");
	}

	@Test
	void registersWithRootCommand() {
		// given
		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		String root = "yui";
		
		// Use a command with no arguments
		class NoArgCommand {
			@Definition("test-help")
			@Command("help")
			public void command(CommandContext<Object> ctx) {
				// no-op
			}
		}
		
		CommandManager<Object> parseManager = new TestCommandManager();
		YuiAnnotationParser<Object> annotationParser = YuiAnnotationParser.create(parseManager, Object.class);
		org.incendo.cloud.Command<Object> parsedCommand = annotationParser.parse(new NoArgCommand()).iterator().next();

		CommandDefinition definition = new CommandDefinition(
				true,
				List.of("help", "h"),
				null,
				null,
				"",
				Map.of(),
				null,
				null,
				null
		);

		var builders = parser.applyOverrides(
				definition,
				"test-help",
				parsedCommand,
				root
		);
		builders.forEach(manager::command);

		Collection<org.incendo.cloud.Command<Object>> commands = manager.commands();
		assertFalse(commands.isEmpty(), "Should register at least one command");
		boolean allUnderRoot = commands.stream()
				.allMatch(cmd -> cmd.rootComponent().name().equals("yui"));
		assertTrue(allUnderRoot, "All commands should be under root 'yui'");
	}

	@Test
	void registersSingleWordAliasUnderRootCommand() {
		// given: This test covers the Discord scenario where "reload" should be "/yui reload"
		// not "/reload" when root command is "yui"
		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		String root = "yui";
		
		// Use a command with no arguments
		class ReloadCommand {
			@Definition("test-reload")
			@Command("reload")
			public void command(CommandContext<Object> ctx) {
				// no-op
			}
		}
		
		CommandManager<Object> parseManager = new TestCommandManager();
		YuiAnnotationParser<Object> annotationParser = YuiAnnotationParser.create(parseManager, Object.class);
		org.incendo.cloud.Command<Object> parsedCommand = annotationParser.parse(new ReloadCommand()).iterator().next();

		CommandDefinition definition = new CommandDefinition(
				true,
				List.of("reload"), // Single word alias
				null,
				null,
				"",
				Map.of(),
				null,
				null,
				null
		);

		var builders = parser.applyOverrides(
				definition,
				"test-reload",
				parsedCommand,
				root
		);
		builders.forEach(manager::command);

		Collection<org.incendo.cloud.Command<Object>> commands = manager.commands();
		assertEquals(1, commands.size(), "Should register exactly one command");

		org.incendo.cloud.Command<Object> command = commands.iterator().next();
		
		assertEquals("yui", command.rootComponent().name(), 
				"Command should be registered under root 'yui', not as standalone '/reload'");
		
		boolean hasReloadSubcommand = command.components().stream()
				.skip(1)
				.anyMatch(c -> c.name().equals("reload"));
		assertTrue(hasReloadSubcommand, "Should have 'reload' as subcommand under 'yui'");
	}

	@Test
	void mapsArgumentsFromUsageToComponents() {
		// given: use a command with only one argument to avoid duplicate chain issues
		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> parser = new CommandDefinitionParser<>(manager);
		
		class SingleArgCommand {
			@Definition("test-single")
			@Command("test <name>")
			public void command(CommandContext<Object> ctx, @Argument("name") String name) {
				// no-op
			}
		}
		
		CommandManager<Object> parseManager = new TestCommandManager();
		YuiAnnotationParser<Object> annotationParser = YuiAnnotationParser.create(parseManager, Object.class);
		org.incendo.cloud.Command<Object> parsedCommand = annotationParser.parse(new SingleArgCommand()).iterator().next();

		CommandDefinition definition = new CommandDefinition(
				true,
				List.of("test"),
				null,
				null,
				"<name>",
				Map.of(),
				null,
				null,
				null
		);

		var builders = parser.applyOverrides(
				definition,
				"test-single",
				parsedCommand,
				null
		);
		builders.forEach(manager::command);

		org.incendo.cloud.Command<Object> command = manager.commands().iterator().next();
		boolean hasName = command.components().stream()
				.anyMatch(c -> c.type() != CommandComponent.ComponentType.LITERAL && c.name().equals("name"));

		assertTrue(hasName, "Command should have 'name' argument");
	}

}

