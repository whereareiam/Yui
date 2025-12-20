package me.whereareiam.yui.common.config.template;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.model.command.CommandCooldown;
import me.whereareiam.yui.model.config.Commands;
import me.whereareiam.yui.model.requirement.Requirements;
import me.whereareiam.yui.model.requirement.type.RoleRequirement;
import me.whereareiam.yui.type.CommandCategory;
import me.whereareiam.yui.type.requirement.RequirementCondition;
import me.whereareiam.yui.type.requirement.RequirementOperator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CommandsTemplate implements TemplateProvider<Commands> {
	@Override
	public Commands supply(Commands commands) {
		// Default values
		CommandDefinition main = new CommandDefinition(
				true,
				List.of("yui"),
				"translate(commands.main.description)",
				"translate(commands.main.example)",
				"{alias}",
				Map.of(),
				CommandCategory.NONE,
				new CommandCooldown(false, 5, ""),
				null
		);

		CommandDefinition help = new CommandDefinition(
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

		CommandDefinition clear = new CommandDefinition(
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

		// Create requirements for reload command
		Requirements reloadRequirements = new Requirements();
		reloadRequirements.setOperator(RequirementOperator.AND);

		RoleRequirement reloadRoleRequirement = new RoleRequirement();
		reloadRoleRequirement.setRoles(List.of("EXAMPLE"));
		reloadRoleRequirement.setRoleMatchBy("NAME");
		reloadRequirements.getGroups().put("ROLE", reloadRoleRequirement);

		CommandDefinition reload = new CommandDefinition(
				true,
				List.of("reload"),
				"translate(commands.reload.description)",
				"translate(commands.reload.example)",
				"{command} {alias}",
				Map.of(),
				CommandCategory.ADMINISTRATION,
				new CommandCooldown(false, 30, ""),
				reloadRequirements
		);

		CommandDefinition plugin = new CommandDefinition(
				true,
				List.of("plugin", "plugins"),
				"translate(commands.plugin.description)",
				"translate(commands.plugin.example)",
				"{command} {alias} [action] [plugin]",
				Map.of(
						"action", "translate(commands.plugin.variables.action)",
						"plugin", "translate(commands.plugin.variables.plugin)"
				),
				CommandCategory.ADMINISTRATION,
				new CommandCooldown(false, 5, ""),
				reloadRequirements
		);

		// Create requirements for language command
		Requirements languageRequirements = new Requirements();
		languageRequirements.setOperator(RequirementOperator.AND);

		RoleRequirement languageRoleRequirement = new RoleRequirement();
		languageRoleRequirement.setCondition(RequirementCondition.HAS);
		languageRoleRequirement.setExpected(true);
		languageRoleRequirement.setRoles(List.of("verified"));
		languageRoleRequirement.setRoleMatchBy("NAME");
		languageRequirements.getGroups().put("ROLE", languageRoleRequirement);

		CommandDefinition language = new CommandDefinition(
				true,
				List.of("language", "lang"),
				"translate(commands.language.description)",
				"translate(commands.language.example)",
				"{alias}",
				Map.of(),
				CommandCategory.UTILITY,
				new CommandCooldown(false, 5, ""),
				languageRequirements
		);

		commands.getCommands().put("main", main);
		commands.getCommands().put("help", help);
		commands.getCommands().put("clear", clear);
		commands.getCommands().put("reload", reload);
		commands.getCommands().put("plugin", plugin);
		commands.getCommands().put("language", language);

		return commands;
	}
}
