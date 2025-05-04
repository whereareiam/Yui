package me.whereareiam.yue.adapter.config.template;

import me.whereareiam.yue.api.model.config.messages.CommandMessages;
import me.whereareiam.yue.api.model.config.messages.GeneralMessages;
import me.whereareiam.yue.api.model.config.messages.Messages;
import me.whereareiam.yue.api.model.config.messages.VocabularyMessages;
import me.whereareiam.yue.api.model.config.messages.command.HelpCommandMessages;
import me.whereareiam.yue.api.model.config.messages.command.MainCommandMessages;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessagesTemplate implements DefaultConfig<Messages> {
	@Override
	public Messages getDefault() {
		Messages messages = new Messages();

		// Default values
		GeneralMessages generalMessages = new GeneralMessages();
		GeneralMessages.TemporaryChannels temporaryChannels = new GeneralMessages.TemporaryChannels();
		GeneralMessages.TemporaryChannels.Close close = new GeneralMessages.TemporaryChannels.Close();
		close.setTitle("Temporary Channel Closing");
		close.setDescription("This temporary channel will be closed in *{0}* seconds.");
		temporaryChannels.setClose(close);

		generalMessages.setTemporaryChannels(temporaryChannels);
		messages.setGeneral(generalMessages);

		CommandMessages commandMessages = new CommandMessages();
		CommandMessages.ErrorMessages errorMessages = new CommandMessages.ErrorMessages();
		errorMessages.setException("An unexpected error occurred. Please try again later.");
		commandMessages.setError(errorMessages);

		MainCommandMessages mainCommand = new MainCommandMessages();
		mainCommand.setDescription("The main command for the bot. In most cases, it is used as a prefix for other commands.");
		mainCommand.setExample("/yue");
		commandMessages.setMain(mainCommand);

		HelpCommandMessages helpCommand = getHelpCommandMessages();
		commandMessages.setHelp(helpCommand);

		messages.setCommands(commandMessages);

		VocabularyMessages vocabulary = getVocabularyMessages();
		messages.setVocabulary(vocabulary);

		return messages;
	}

	@NotNull
	private static HelpCommandMessages getHelpCommandMessages() {
		HelpCommandMessages helpCommand = new HelpCommandMessages();
		helpCommand.setDescription("Shows all commands organized by categories with their descriptions and usage examples.");
		helpCommand.setExample("/yue help UTILITY");
		helpCommand.setVariables(Map.of(
				"category", "Displays the list of commands in the specified category."
		));

		HelpCommandMessages.Information information = new HelpCommandMessages.Information();
		HelpCommandMessages.Information.Global global = new HelpCommandMessages.Information.Global();
		global.setTitle("Help Information");
		global.setDescription("Select a category to see the commands in it.");
		information.setGlobal(global);

		HelpCommandMessages.Information.Specific specific = new HelpCommandMessages.Information.Specific();
		specific.setTitle("Help Information");
		specific.setDescription("List of commands in the \"{0}\" category.");
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

	private static VocabularyMessages getVocabularyMessages() {
		VocabularyMessages vocabulary = new VocabularyMessages();
		vocabulary.setCancel("Cancel");
		vocabulary.setProceed("Continue");

		VocabularyMessages.Category category = new VocabularyMessages.Category();
		category.setUtility("🛠️ Utility");
		category.setFun("🎮 Fun");
		category.setModeration("🛡️ Moderation");
		category.setAdministration("⚙️ Administration");
		category.setNone("📌 None");
		vocabulary.setCategory(category);

		return vocabulary;
	}
}
