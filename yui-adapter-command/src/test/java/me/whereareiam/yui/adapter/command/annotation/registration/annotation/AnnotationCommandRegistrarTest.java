package me.whereareiam.yui.adapter.command.annotation.registration.annotation;

import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.adapter.command.registration.annotation.AnnotationCommandRegistrar;
import me.whereareiam.yui.adapter.command.registration.CommandDefinitionParser;
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
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests for {@link AnnotationCommandRegistrar} + {@link CommandDefinitionParser}.
 */
class AnnotationCommandRegistrarTest {
	private static final class TestCommandManager extends CommandManager<Object> {
		TestCommandManager() {
			super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
		}

		@Override
		public boolean hasPermission(@NotNull Object sender, @NotNull String permission) {
			return true;
		}
	}

	private static final class SimpleCommands {
		@Definition("test-main")
		@Command("ignored <name>")
		public void root(CommandContext<Object> ctx, @Argument("name") String name) {
			// no-op
		}
	}

	/**
	 * Multi-word aliases with subcommand-style usage using {command} placeholder.
	 */
	private static final class SubcommandCommands {
		@Definition("main")
		@Command("ignored")
		public void root(CommandContext<Object> ctx) {
			// no-op
		}

		@Definition("main-help")
		@Command("ignored help")
		public void help(CommandContext<Object> ctx) {
			// no-op
		}
	}

	@Test
	void registersCommandsBasedOnCommandDefinition() {
		// given: a real command manager and definition-driven registrar
		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> definitionParser = new CommandDefinitionParser<>(manager);

		CommandDefinition definition = new CommandDefinition(
				true, // enabled
				List.of("cfgmain", "cfgm"), // aliases
				"Config-based description", // description
				null, // example
				"<name>", // usage
				Map.of(), // variables
				null, // category
				null, // cooldown
				null // requirements
		);

		Function<String, CommandDefinition> definitionLookup = id -> {
			if (id.equals("test-main")) return definition;
			return null;
		};

		AnnotationCommandRegistrar<Object> registrar = new AnnotationCommandRegistrar<>(
				definitionParser,
				Object.class,
				definitionLookup
		);

		// when: registering the annotated container
		registrar.register(new SimpleCommands());

		// then: exactly one final command should be present on the real manager
		Collection<org.incendo.cloud.Command<Object>> commands = manager.commands();
		assertEquals(1, commands.size(), "Exactly one command should be registered from definition");
		org.incendo.cloud.Command<Object> command = commands.iterator().next();

		// root literal should be the primary alias from the definition (cfgmain)
		assertEquals("cfgmain", command.rootComponent().name());

		// argument "name" should exist as a non-literal component
		assertTrue(
				command.components().stream().anyMatch(c ->
						c.type() != CommandComponent.ComponentType.LITERAL && c.name().equals("name")),
				"Argument 'name' should be registered as a non-literal component"
		);

		// definition meta should be set from @Definition via CommandDefinitionParser
		CloudKey<String> key = YuiCommandMetaKeys.DEFINITION;
		String definitionId = command.commandMeta().optional(key).orElse(null);
		assertEquals("test-main", definitionId, "Definition meta should match @Definition value");

		// registrar.resolveDefinition should also work
		Optional<CommandDefinition> resolved = registrar.resolveDefinition(command);
		assertTrue(resolved.isPresent(), "resolveDefinition should return a definition");
		assertSame(definition, resolved.get(), "Resolved definition instance should match");
	}

	@Test
	void honorsDisabledDefinitions() {
		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> definitionParser = new CommandDefinitionParser<>(manager);

		// disabled definition
		CommandDefinition disabled = new CommandDefinition(
				false,
				List.of("disabled"),
				null,
				null,
				"<name>",
				Map.of(),
				null,
				null,
				null
		);

		Function<String, CommandDefinition> lookup = _ -> disabled;

		AnnotationCommandRegistrar<Object> registrar = new AnnotationCommandRegistrar<>(
				definitionParser,
				Object.class,
				lookup
		);

		registrar.register(new SimpleCommands());

		// no commands should be registered because definition is disabled
		assertEquals(0, manager.commands().size(), "Disabled definition should not register commands");
	}

	@Test
	void registersSubcommandBasedOnRootAndPlaceholderUsage() {
		CommandManager<Object> manager = new TestCommandManager();
		CommandDefinitionParser<Object> definitionParser = new CommandDefinitionParser<>(manager);

		// root definition with {command} placeholder in usage
		CommandDefinition rootDef = new CommandDefinition(
				true,
				List.of("yui"),
				null,
				null,
				"{command}",
				Map.of(),
				null,
				null,
				null
		);

		// subcommand definition
		CommandDefinition helpDef = new CommandDefinition(
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

		Function<String, CommandDefinition> lookup = id -> switch (id) {
			case "main" -> rootDef;
			case "main-help" -> helpDef;
			default -> null;
		};

		AnnotationCommandRegistrar<Object> registrar = new AnnotationCommandRegistrar<>(
				definitionParser,
				Object.class,
				lookup
		);

		registrar.setRootCommand("yui");
		registrar.register(new SubcommandCommands());

		// we expect at least the root command to exist
		Collection<org.incendo.cloud.Command<Object>> commands = manager.commands();
		assertFalse(commands.isEmpty(), "At least one command should be registered for subcommand setup");

		// there should be a command whose root is 'yui'
		boolean hasRoot = commands.stream().anyMatch(cmd -> cmd.rootComponent().name().equals("yui"));
		assertTrue(hasRoot, "Root command 'yui' should be registered");
	}
}
