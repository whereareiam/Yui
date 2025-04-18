package me.whereareiam.yue.adapter.config.template;

import me.whereareiam.yue.api.model.config.messages.CommandMessages;
import me.whereareiam.yue.api.model.config.messages.Messages;
import me.whereareiam.yue.api.model.config.messages.VocabularyMessages;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessagesTemplate implements DefaultConfig<Messages> {
	@Override
	public Messages getDefault() {
		Messages messages = new Messages();

		// Default values
		CommandMessages commandMessages = new CommandMessages();
		CommandMessages.MainCommand mainCommand = new CommandMessages.MainCommand();
		mainCommand.setDescription("The main command for the bot. In most cases, it is used as a prefix for other commands.");
		commandMessages.setMain(mainCommand);

		CommandMessages.HelpCommand helpCommand = new CommandMessages.HelpCommand();
		helpCommand.setDescription("Shows all commands organized by categories with their descriptions and usage examples.");
		helpCommand.setVariables(Map.of(
				"category", "Displays the list of commands in the specified category."
		));
		commandMessages.setHelp(helpCommand);

		messages.setCommands(commandMessages);

		VocabularyMessages vocabulary = new VocabularyMessages();
		vocabulary.setCancel("Cancel");

		messages.setVocabulary(vocabulary);

		return messages;
	}
}
