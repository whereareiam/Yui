package me.whereareiam.yue.adapter.config.template;

import me.whereareiam.yue.api.model.command.Command;
import me.whereareiam.yue.api.model.command.CommandCooldown;
import me.whereareiam.yue.api.model.config.Commands;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import me.whereareiam.yue.api.type.CommandCategory;

import java.util.List;
import java.util.Map;

public class CommandsTemplate implements DefaultConfig<Commands> {
	@Override
	public Commands getDefault() {
		Commands commands = new Commands();

		// Default values
		Command main = new Command(
				false,
				List.of("yue"),
				"translate(commands.main.description)",
				"translate(commands.main.example)",
				"{alias}",
				Map.of(),
				CommandCategory.NONE,
				new CommandCooldown(false, 5, "")
		);

		Command help = new Command(
				true,
				List.of("help"),
				"translate(commands.help.description)",
				"translate(commands.help.example)",
				"{alias} [category]",
				Map.of(
						"category", "translate(commands.help.variables.category)"
				),
				CommandCategory.UTILITY,
				new CommandCooldown(false, 5, "")
		);

		commands.getCommands().put("main", main);
		commands.getCommands().put("help", help);

		return commands;
	}
}
