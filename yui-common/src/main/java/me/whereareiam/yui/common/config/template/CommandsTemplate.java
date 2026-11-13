package me.whereareiam.yui.common.config.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.yui.model.command.CommandCooldown;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.model.config.Commands;
import me.whereareiam.yui.model.type.Duration;
import me.whereareiam.yui.type.CommandCategory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Yui's commands as {@code commands.yml} has them unless the file changes them. Server commands need an operator,
 * {@code /clear} a moderator in a text channel or thread of the guild; core's defaults never name a plugin's
 * condition, an installation tightens them in its own file.
 */
@Component
@RequiredArgsConstructor
public class CommandsTemplate implements DefaultsProvider<Commands> {
	private static final String OPERATOR = "- audience: operator";

	private final Configura configura;

	@Override
	public Commands supply(Commands commands) {
		command("main", List.of("yui"), "{alias}", CommandCategory.NONE, 5).into(commands);
		command("help", List.of("help"), "{alias} [category]", CommandCategory.UTILITY, 5)
				.into(commands);
		command("clear", List.of("clear"), "{command} {alias} <user>", CommandCategory.ADMINISTRATION, 10)
				.requires("""
						- audience: moderator
						- scope: guild
						- channel-type: [text, thread]
						""")
				.into(commands);
		command("reload", List.of("reload"), "{command} {alias}", CommandCategory.ADMINISTRATION, 30).requires(OPERATOR).into(commands);
		command("plugin", List.of("plugin", "plugins"), "{command} {alias} [action] [plugin]", CommandCategory.ADMINISTRATION, 5)
				.requires(OPERATOR)
				.into(commands);
		command("status", List.of("status"), "{command} {alias}", CommandCategory.ADMINISTRATION, 5).requires(OPERATOR).into(commands);
		command("language", List.of("language", "lang"), "{alias}", CommandCategory.UTILITY, 5).into(commands);
		command("update-check", List.of("update", "check-update"), "{command} {alias} <target>", CommandCategory.ADMINISTRATION, 10)
				.requires(OPERATOR)
				.into(commands);

		return commands;
	}

	private Draft command(String id, List<String> aliases, String usage, CommandCategory category, int cooldown) {
		return new Draft(id, aliases, usage, category, cooldown);
	}

	// One default command, written step by step.
	private final class Draft {
		private final String id;
		private final List<String> aliases;
		private final String usage;
		private final CommandCategory category;
		private final int cooldown;
		private Requirements requires = Requirements.none();

		private Draft(String id, List<String> aliases, String usage, CommandCategory category, int cooldown) {
			this.id = id;
			this.aliases = aliases;
			this.usage = usage;
			this.category = category;
			this.cooldown = cooldown;
		}

		Draft requires(String yaml) {
			try {
				requires = configura.mapper().readValue(yaml, Requirements.class);
			} catch (JsonProcessingException exception) {
				throw new IllegalStateException("Yui's own requirements of " + id + " cannot be read", exception);
			}

			return this;
		}

		void into(Commands commands) {
			commands.getCommands().put(id, new CommandDefinition(
					true,
					aliases,
					usage,
					category,
					new CommandCooldown(false, Duration.ofSeconds(cooldown), null),
					requires,
					false
			));
		}
	}
}
