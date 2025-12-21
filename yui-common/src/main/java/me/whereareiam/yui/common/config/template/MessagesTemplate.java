package me.whereareiam.yui.common.config.template;

import lombok.RequiredArgsConstructor;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.common.config.template.messages.ErrorMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.GeneralMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.*;
import me.whereareiam.yui.model.config.messages.CommandMessages;
import me.whereareiam.yui.model.config.messages.GeneralMessages;
import me.whereareiam.yui.model.config.messages.Messages;
import me.whereareiam.yui.model.config.messages.command.*;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class MessagesTemplate implements TemplateProvider<Messages> {
	private final GeneralMessagesTemplate generalTemplate;
	private final ErrorMessagesTemplate errorTemplate;
	private final MainCommandMessagesTemplate mainTemplate;
	private final HelpCommandMessagesTemplate helpTemplate;
	private final ClearCommandMessagesTemplate clearTemplate;
	private final ReloadCommandMessagesTemplate reloadTemplate;
	private final PluginCommandMessagesTemplate pluginTemplate;
	private final LanguageCommandMessagesTemplate languageTemplate;

	@Override
	public Messages supply(Messages messages) {
		messages.setGeneral(supply(generalTemplate, GeneralMessages::new));

		CommandMessages commandMessages = new CommandMessages();
		commandMessages.setError(supply(errorTemplate, CommandMessages.ErrorMessages::new));
		commandMessages.setMain(supply(mainTemplate, MainCommandMessages::new));
		commandMessages.setHelp(supply(helpTemplate, HelpCommandMessages::new));
		commandMessages.setClear(supply(clearTemplate, ClearCommandMessages::new));
		commandMessages.setReload(supply(reloadTemplate, ReloadCommandMessages::new));
		commandMessages.setPlugin(supply(pluginTemplate, PluginCommandMessages::new));
		commandMessages.setLanguage(supply(languageTemplate, LanguageCommandMessages::new));
		messages.setCommands(commandMessages);

		return messages;
	}

	private <T> T supply(TemplateProvider<T> provider, Supplier<T> supplier) {
		return provider.supply(supplier.get());
	}
}
