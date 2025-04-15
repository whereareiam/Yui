package me.whereareiam.yue.api.model.command;

public class CommandCooldown {
	private boolean enabled;
	private int cooldown;
	private String group;

	public CommandCooldown() {
	}

	public CommandCooldown(boolean enabled, int cooldown, String group) {
		this.enabled = enabled;
		this.cooldown = cooldown;
		this.group = group;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getCooldown() {
		return cooldown;
	}

	public String getGroup() {
		return group;
	}
}
