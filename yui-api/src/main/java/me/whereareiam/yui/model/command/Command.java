package me.whereareiam.yui.model.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.whereareiam.yui.model.requirement.Requirements;
import me.whereareiam.yui.type.CommandCategory;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Command {
	private boolean enabled;
	private List<String> aliases;
	private String description;
	private String example;
	private String usage;

	private Map<String, String> variables;

	private CommandCategory category;
	private CommandCooldown cooldown;

	// Optional requirements block; if null, the command is considered unrestricted
	private Requirements requirements;
}
