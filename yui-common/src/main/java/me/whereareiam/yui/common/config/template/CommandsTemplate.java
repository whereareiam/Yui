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

import java.util.LinkedHashMap;
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
		clearRequirements.setGroups(new LinkedHashMap<>());

		RoleRequirement roleRequirement = new RoleRequirement();
		roleRequirement.setRoles(List.of("EXAMPLE"));
		roleRequirement.setRoleMatchBy("NAME");
		clearRequirements.getGroups().put("ROLE", roleRequirement);

		CommandDefinition clear = new CommandDefinition(
				true,
				List.of("clear"),
				"translate(commands.clear.description)",
				"translate(commands.clear.example)",
				"{command} {alias} <user>",
				Map.of(
						"user", "translate(commands.clear.variables.user)"
				),
				CommandCategory.ADMINISTRATION,
				new CommandCooldown(false, 10, ""),
				clearRequirements
		);

		// Create requirements for reload command
		Requirements reloadRequirements = new Requirements();
		reloadRequirements.setOperator(RequirementOperator.AND);
		reloadRequirements.setGroups(new LinkedHashMap<>());

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

		// Create requirements for plugin command
		Requirements pluginRequirements = new Requirements();
		pluginRequirements.setOperator(RequirementOperator.AND);
		pluginRequirements.setGroups(new LinkedHashMap<>());

		RoleRequirement pluginRoleRequirement = new RoleRequirement();
		pluginRoleRequirement.setRoles(List.of("EXAMPLE"));
		pluginRoleRequirement.setRoleMatchBy("NAME");
		pluginRequirements.getGroups().put("ROLE", pluginRoleRequirement);

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
				pluginRequirements
		);

		// Create requirements for status command
		Requirements statusRequirements = new Requirements();
		statusRequirements.setOperator(RequirementOperator.AND);
		statusRequirements.setGroups(new LinkedHashMap<>());

		RoleRequirement statusRoleRequirement = new RoleRequirement();
		statusRoleRequirement.setRoles(List.of("EXAMPLE"));
		statusRoleRequirement.setRoleMatchBy("NAME");
		statusRequirements.getGroups().put("ROLE", statusRoleRequirement);

		CommandDefinition status = new CommandDefinition(
				true,
				List.of("status"),
				"translate(commands.status.description)",
				"translate(commands.status.example)",
				"{command} {alias}",
				Map.of(),
				CommandCategory.ADMINISTRATION,
				new CommandCooldown(false, 5, ""),
				statusRequirements
		);

		// Create requirements for language command
		Requirements languageRequirements = new Requirements();
		languageRequirements.setOperator(RequirementOperator.AND);
		languageRequirements.setGroups(new LinkedHashMap<>());

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

		// Create requirements for update-check command
		Requirements updateCheckRequirements = new Requirements();
		updateCheckRequirements.setOperator(RequirementOperator.AND);
		updateCheckRequirements.setGroups(new LinkedHashMap<>());

		RoleRequirement updateCheckRoleRequirement = new RoleRequirement();
		updateCheckRoleRequirement.setRoles(List.of("EXAMPLE"));
		updateCheckRoleRequirement.setRoleMatchBy("NAME");
		updateCheckRequirements.getGroups().put("ROLE", updateCheckRoleRequirement);

		CommandDefinition updateCheck = new CommandDefinition(
				true,
				List.of("update", "check-update"),
				"translate(commands.update_check.description)",
				"translate(commands.update_check.example)",
				"{command} {alias} <target>",
				Map.of(
						"target", "translate(commands.update_check.variables.target)"
				),
				CommandCategory.ADMINISTRATION,
				new CommandCooldown(false, 10, ""),
				updateCheckRequirements
		);

		commands.getCommands().put("main", main);
		commands.getCommands().put("help", help);
		commands.getCommands().put("clear", clear);
		commands.getCommands().put("reload", reload);
		commands.getCommands().put("plugin", plugin);
		commands.getCommands().put("status", status);
		commands.getCommands().put("language", language);
		commands.getCommands().put("update-check", updateCheck);

		return commands;
	}
}
