package me.whereareiam.yue.api.output.service;

import me.whereareiam.yue.api.model.command.Command;
import me.whereareiam.yue.api.model.config.Commands;

import java.util.Map;

public interface CommandService {
	/**
	 * Register a single command (by name) into the service’s in-memory storage.
	 */
	void register(String commandName, Command command);

	/**
	 * Register multiple commands (by name->command) at once.
	 */
	void register(Map<String, Command> commands);

	/**
	 * Register multiple commands from a Commands wrapper object.
	 */
	void register(Commands commands);

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
