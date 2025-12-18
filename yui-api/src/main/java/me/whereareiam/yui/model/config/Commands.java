package me.whereareiam.yui.model.config;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.command.CommandDefinition;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Commands {
	private Map<String, CommandDefinition> commands = new HashMap<>();
}
