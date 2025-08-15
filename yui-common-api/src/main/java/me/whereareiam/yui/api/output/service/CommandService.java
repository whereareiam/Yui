package me.whereareiam.yui.api.output.service;

import me.whereareiam.yui.api.model.command.Command;
import me.whereareiam.yui.api.model.config.Commands;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public interface CommandService {

	/**
	 * Register a single command (by name) into the service’s in-memory storage.
	 */
	void register(ApplicationContext context, String commandName, Command command);

	/**
	 * Register multiple commands (by name->command) at once.
	 */
	void register(ApplicationContext context, Map<String, Command> commands);

	/**
	 * Register multiple commands from a Commands wrapper object.
	 */
	void register(ApplicationContext context, Commands commands);

	/**
	 * Unregister/remove a command from in-memory storage.
	 */
	void unregister(String commandName);

	/**
	 * Retrieve a single command by name. Returns null if not found.
	 */
	Command getCommand(String commandName);

	/**
	 * Return an unmodifiable map of all known commands.
	 */
	Map<String, Command> getCommands();
}
