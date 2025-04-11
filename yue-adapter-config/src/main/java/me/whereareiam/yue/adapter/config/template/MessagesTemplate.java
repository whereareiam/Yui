package me.whereareiam.yue.adapter.config.template;

import me.whereareiam.yue.api.model.config.messages.Messages;
import me.whereareiam.yue.api.model.config.messages.VocabularyMessages;
import me.whereareiam.yue.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class MessagesTemplate implements DefaultConfig<Messages> {
	@Override
	public Messages getDefault() {
		Messages messages = new Messages();

		// Default values
		VocabularyMessages vocabulary = new VocabularyMessages();
		vocabulary.setCancel("Cancel");

		messages.setVocabulary(vocabulary);

		return messages;
	}
}
