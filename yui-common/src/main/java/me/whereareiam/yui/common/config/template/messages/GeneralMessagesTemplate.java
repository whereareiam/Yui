package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.messages.GeneralMessages;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeneralMessagesTemplate implements TemplateProvider<GeneralMessages> {
	@Override
	public GeneralMessages supply(GeneralMessages generalMessages) {
		GeneralMessages.Conversation conversation = new GeneralMessages.Conversation();
		GeneralMessages.Conversation.Close close = new GeneralMessages.Conversation.Close();
		close.setTitle("Closing conversation");
		close.setDescription(List.of("This conversation will be closed in *<p:seconds>* seconds."));
		conversation.setClose(close);

		generalMessages.setConversation(conversation);
		
		return generalMessages;
	}
}