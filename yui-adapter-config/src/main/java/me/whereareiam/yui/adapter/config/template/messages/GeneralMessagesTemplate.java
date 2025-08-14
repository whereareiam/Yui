package me.whereareiam.yui.adapter.config.template.messages;

import me.whereareiam.yui.api.model.config.messages.GeneralMessages;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class GeneralMessagesTemplate implements DefaultConfig<GeneralMessages> {
	@Override
	public GeneralMessages getDefault() {
		GeneralMessages generalMessages = new GeneralMessages();
		GeneralMessages.TemporaryChannels temporaryChannels = new GeneralMessages.TemporaryChannels();
		GeneralMessages.TemporaryChannels.Close close = new GeneralMessages.TemporaryChannels.Close();
		close.setTitle("Temporary Channel Closing");
		close.setDescription("This temporary channel will be closed in *{0}* seconds.");
		temporaryChannels.setClose(close);

		generalMessages.setTemporaryChannels(temporaryChannels);
		return generalMessages;
	}
}


