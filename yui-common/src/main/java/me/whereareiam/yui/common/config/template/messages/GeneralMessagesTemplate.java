package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.messages.GeneralMessages;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeneralMessagesTemplate implements TemplateProvider<GeneralMessages> {
	@Override
	public GeneralMessages supply(GeneralMessages generalMessages) {
		GeneralMessages.TemporaryChannels temporaryChannels = new GeneralMessages.TemporaryChannels();
		GeneralMessages.TemporaryChannels.Close close = new GeneralMessages.TemporaryChannels.Close();
		close.setTitle("Temporary Channel Closing");
		close.setDescription(List.of("This temporary channel will be closed in *<p:seconds>* seconds."));
		temporaryChannels.setClose(close);

		generalMessages.setTemporaryChannels(temporaryChannels);
		
		return generalMessages;
	}
}


