package me.whereareiam.yui.common.config.template;

import lombok.RequiredArgsConstructor;
import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.common.config.template.messages.AuditMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.ErrorMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.GeneralMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.*;
import me.whereareiam.yui.localization.format.FileFormat;
import me.whereareiam.yui.localization.format.FileFormats;
import me.whereareiam.yui.localization.provider.LocalizationProvider;
import me.whereareiam.yui.model.config.messages.AuditMessages;
import me.whereareiam.yui.model.config.messages.CommandMessages;
import me.whereareiam.yui.model.config.messages.GeneralMessages;
import me.whereareiam.yui.model.config.messages.Messages;
import me.whereareiam.yui.model.config.messages.command.*;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class MessagesTemplate implements LocalizationProvider<Messages> {
	private final GeneralMessagesTemplate generalTemplate;
	private final AuditMessagesTemplate auditTemplate;
	private final ErrorMessagesTemplate errorTemplate;
	private final MainCommandMessagesTemplate mainTemplate;
	private final HelpCommandMessagesTemplate helpTemplate;
	private final ClearCommandMessagesTemplate clearTemplate;
	private final ReloadCommandMessagesTemplate reloadTemplate;
	private final PluginCommandMessagesTemplate pluginTemplate;
	private final LanguageCommandMessagesTemplate languageTemplate;
	private final UpdateCheckCommandMessagesTemplate updateCheckTemplate;

	@Override
	public Class<Messages> getModelClass() {
		return Messages.class;
	}

	@Override
	public FileFormat getFormat() {
		return FileFormats.LOCALE;
	}

	@Override
	public boolean applyOnce() {
		return false;
	}

	@Override
	public Messages supply(Messages messages) {
		messages.setGeneral(supply(generalTemplate, GeneralMessages::new));
		messages.setAudit(supply(auditTemplate, AuditMessages::new));

		CommandMessages commandMessages = new CommandMessages();
		commandMessages.setError(supply(errorTemplate, CommandMessages.ErrorMessages::new));
		commandMessages.setMain(supply(mainTemplate, MainCommandMessages::new));
		commandMessages.setHelp(supply(helpTemplate, HelpCommandMessages::new));
		commandMessages.setClear(supply(clearTemplate, ClearCommandMessages::new));
		commandMessages.setReload(supply(reloadTemplate, ReloadCommandMessages::new));
		commandMessages.setPlugin(supply(pluginTemplate, PluginCommandMessages::new));
		commandMessages.setLanguage(supply(languageTemplate, LanguageCommandMessages::new));
		commandMessages.setUpdateCheck(supply(updateCheckTemplate, UpdateCheckCommandMessages::new));
		messages.setCommands(commandMessages);

		return messages;
	}

	private <T> T supply(TemplateProvider<T> provider, Supplier<T> supplier) {
		return provider.supply(supplier.get());
	}
}
