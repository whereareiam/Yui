package me.whereareiam.yui.adapter.command.parsing.annotation;

import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.key.CloudKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class YuiAnnotationParserTest {
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
		@Command("main <name>")
		public void root(CommandContext<Object> ctx, @Argument("name") String name) {
			// no-op
		}
	}

	@Test
	void parsesYuiAnnotationsIntoCloudCommands() {
		// given
		CommandManager<Object> manager = new TestCommandManager();
		YuiAnnotationParser<Object> parser = YuiAnnotationParser.create(manager, Object.class);

		// when
		Collection<org.incendo.cloud.Command<Object>> commands = parser.parse(new TestCommands());

		// then
		assertEquals(1, commands.size(), "Exactly one command should be parsed");
		org.incendo.cloud.Command<Object> command = commands.iterator().next();

		// root literal should be 'main'
		assertEquals("main", command.rootComponent().name());

		// argument "name" should exist as a non-literal component
		assertTrue(
				command.components().stream().anyMatch(c ->
						c.type() != CommandComponent.ComponentType.LITERAL && c.name().equals("name")),
				"Argument 'name' should be registered as a non-literal component"
		);

		// definition meta should be set from @Definition
		CloudKey<String> key = YuiCommandMetaKeys.DEFINITION;
		String definitionId = command.commandMeta().optional(key).orElse(null);
		assertEquals("test-main", definitionId, "Definition meta should match @Definition value");

		// ensure the parsed collection is not empty; manager registration is not required here
		assertFalse(commands.isEmpty(), "Parsed commands should not be empty");
	}
}

