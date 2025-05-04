package me.whereareiam.yui.api.model.config;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.model.command.Command;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Commands {
	private Map<String, Command> commands = new HashMap<>();

	public void addCommand(String name, Command command) {
		commands.put(name, command);
	}
}
