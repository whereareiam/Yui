package me.whereareiam.yui.api.command;

import me.whereareiam.yui.api.model.command.CommandDefinition;
import me.whereareiam.yui.api.type.Source;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Supplies command definitions from a particular source (internal or external).
 */
public interface DefinitionProvider {
	/**
	 * Identifier of the source (e.g., "internal", "external:<id>").
	 */
	@NotNull String id();

	@NotNull Source source();

	@NotNull Map<String, CommandDefinition> definitions();
}
