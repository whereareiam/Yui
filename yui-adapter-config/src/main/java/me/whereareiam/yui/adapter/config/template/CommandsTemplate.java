package me.whereareiam.yui.adapter.config.template;

import me.whereareiam.yui.api.model.command.Command;
import me.whereareiam.yui.api.model.command.CommandCooldown;
import me.whereareiam.yui.api.model.config.Commands;
import me.whereareiam.yui.api.model.requirement.Requirements;
import me.whereareiam.yui.api.model.requirement.RoleRequirement;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import me.whereareiam.yui.api.type.CommandCategory;
import me.whereareiam.yui.api.type.RequirementOperator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CommandsTemplate implements DefaultConfig<Commands> {
	@Override
	public Commands getDefault() {
		Commands commands = new Commands();

		// Default values
		Command main = new Command(
				false,
				List.of("yui"),
				"translate(commands.main.description)",
				"translate(commands.main.example)",
				"{alias}",
				Map.of(),
				CommandCategory.NONE,
				new CommandCooldown(false, 5, ""),
				null
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
				new CommandCooldown(false, 5, ""),
				null
		);

		// Create requirements for clear command
		Requirements clearRequirements = new Requirements();
		clearRequirements.setOperator(RequirementOperator.AND);

		RoleRequirement roleRequirement = new RoleRequirement();
		roleRequirement.setRoles(List.of("EXAMPLE"));
		roleRequirement.setRoleMatchBy("NAME");
		clearRequirements.getGroups().put("ROLE", roleRequirement);

		Command clear = new Command(
				true,
				List.of("clear"),
				"translate(commands.clear.description)",
				"translate(commands.clear.example)",
				"{command} {alias} (user)",
				Map.of(
						"user", "The Discord user whose profile should be cleared"
				),
				CommandCategory.ADMINISTRATION,
				new CommandCooldown(false, 10, ""),
				clearRequirements
		);

		commands.getCommands().put("main", main);
		commands.getCommands().put("help", help);
		commands.getCommands().put("clear", clear);

		return commands;
	}
}
