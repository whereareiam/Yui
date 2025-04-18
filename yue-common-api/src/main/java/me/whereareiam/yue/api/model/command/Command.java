package me.whereareiam.yue.api.model.command;

import me.whereareiam.yue.api.type.CommandCategory;

import java.util.List;
import java.util.Map;

public class Command {
	private boolean enabled;
	private List<String> aliases;
	private String description;
	private String usage;

	private Map<String, String> variables;

	private CommandCategory category;
	private CommandCooldown cooldown;

	public Command() {
	}

	public Command(
			boolean enabled,
			List<String> aliases,
			String description,
			String usage, Map<String, String> variables,
			CommandCategory category,
			CommandCooldown cooldown
	) {
		this.enabled = enabled;
		this.aliases = aliases;
		this.description = description;
		this.usage = usage;
		this.variables = variables;
		this.category = category;
		this.cooldown = cooldown;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public String getDescription() {
		return description;
	}

	public String getUsage() {
		return usage;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public CommandCategory getCategory() {
		return category;
	}

	public CommandCooldown getCooldown() {
		return cooldown;
	}


}
