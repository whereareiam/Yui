package me.whereareiam.yui.adapter.config.template.messages.command;

import me.whereareiam.yui.model.config.messages.command.HelpCommandMessages;
import me.whereareiam.yui.config.DefaultConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class HelpCommandMessagesTemplate implements DefaultConfig<HelpCommandMessages> {
	@Override
	public HelpCommandMessages getDefault() {
		return build();
	}

	@NotNull
	private static HelpCommandMessages build() {
		HelpCommandMessages helpCommand = new HelpCommandMessages();
		helpCommand.setDescription("Shows all commands organized by categories with their descriptions and usage examples.");
		helpCommand.setExample("/yui help UTILITY");
		helpCommand.setVariables(Map.of(
				"category", "Displays the list of commands in the specified category."
		));

		HelpCommandMessages.Information information = new HelpCommandMessages.Information();
		HelpCommandMessages.Information.Global global = new HelpCommandMessages.Information.Global();
		global.setTitle("Help Information");
		global.setDescription(List.of("Select a category to see the commands in it."));
		information.setGlobal(global);

		HelpCommandMessages.Information.Specific specific = new HelpCommandMessages.Information.Specific();
		specific.setTitle("Help Information");
		specific.setDescription(List.of("List of commands in the \"{0}\" category."));
		specific.setHeadFormat("Command: {0}");
		specific.setFootFormat("Example: `{0}`\n*Description: {1}*");
		information.setSpecific(specific);

		HelpCommandMessages.Category category = new HelpCommandMessages.Category();
		category.setUtility("Tools and commands for general use and information");
		category.setFun("Entertainment commands to have a good time");
		category.setModeration("Commands to maintain order and manage users");
		category.setAdministration("Advanced settings and server management commands");
		helpCommand.setCategory(category);

		helpCommand.setInformation(information);

		return helpCommand;
	}
}


