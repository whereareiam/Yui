package me.whereareiam.yui.adapter.config.template;

import me.whereareiam.yui.adapter.config.template.messages.ErrorMessagesTemplate;
import me.whereareiam.yui.adapter.config.template.messages.GeneralMessagesTemplate;
import me.whereareiam.yui.adapter.config.template.messages.VocabularyMessagesTemplate;
import me.whereareiam.yui.adapter.config.template.messages.command.*;
import me.whereareiam.yui.api.model.config.messages.CommandMessages;
import me.whereareiam.yui.api.model.config.messages.Messages;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class MessagesTemplate implements DefaultConfig<Messages> {
	@Override
	public Messages getDefault() {
		Messages messages = new Messages();

		messages.setGeneral(new GeneralMessagesTemplate().getDefault());

		// commands
		CommandMessages commandMessages = new CommandMessages();
		commandMessages.setError(new ErrorMessagesTemplate().getDefault());
		commandMessages.setMain(new MainCommandMessagesTemplate().getDefault());
		commandMessages.setHelp(new HelpCommandMessagesTemplate().getDefault());
		commandMessages.setClear(new ClearCommandMessagesTemplate().getDefault());
		commandMessages.setReload(new ReloadCommandMessagesTemplate().getDefault());
		commandMessages.setPlugin(new PluginCommandMessagesTemplate().getDefault());
		messages.setCommands(commandMessages);

		messages.setVocabulary(new VocabularyMessagesTemplate().getDefault());

		return messages;
	}
}
