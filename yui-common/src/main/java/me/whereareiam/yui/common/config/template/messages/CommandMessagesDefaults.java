package me.whereareiam.yui.common.config.template.messages;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.common.config.template.messages.command.AuditCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.ClearCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.HelpCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.LanguageCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.MainCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.PluginCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.ReloadCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.StatusCommandMessagesTemplate;
import me.whereareiam.yui.common.config.template.messages.command.UpdateCheckCommandMessagesTemplate;
import me.whereareiam.yui.message.MessageDefaults;
import me.whereareiam.yui.model.config.messages.CommandMessages;
import me.whereareiam.yui.model.config.messages.command.AuditCommandMessages;
import me.whereareiam.yui.model.config.messages.command.ClearCommandMessages;
import me.whereareiam.yui.model.config.messages.command.HelpCommandMessages;
import me.whereareiam.yui.model.config.messages.command.LanguageCommandMessages;
import me.whereareiam.yui.model.config.messages.command.MainCommandMessages;
import me.whereareiam.yui.model.config.messages.command.PluginCommandMessages;
import me.whereareiam.yui.model.config.messages.command.ReloadCommandMessages;
import me.whereareiam.yui.model.config.messages.command.StatusCommandMessages;
import me.whereareiam.yui.model.config.messages.command.UpdateCheckCommandMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/**
 * The default texts of {@code messages/<locale>/commands.yml}, assembled from each command's own defaults.
 */
public class CommandMessagesDefaults implements MessageDefaults<CommandMessages> {
	private final ErrorMessagesTemplate errorTemplate;
	private final MainCommandMessagesTemplate mainTemplate;
	private final HelpCommandMessagesTemplate helpTemplate;
	private final ClearCommandMessagesTemplate clearTemplate;
	private final ReloadCommandMessagesTemplate reloadTemplate;
	private final PluginCommandMessagesTemplate pluginTemplate;
	private final StatusCommandMessagesTemplate statusTemplate;
	private final LanguageCommandMessagesTemplate languageTemplate;
	private final UpdateCheckCommandMessagesTemplate updateCheckTemplate;
	private final AuditCommandMessagesTemplate auditTemplate;

	@Override
	public @NotNull String file() {
		return "commands";
	}

	@Override
	public @NotNull CommandMessages defaults() {
		CommandMessages messages = new CommandMessages();
		messages.setError(errorTemplate.supply(new CommandMessages.ErrorMessages()));
		messages.setMain(mainTemplate.supply(new MainCommandMessages()));
		messages.setHelp(helpTemplate.supply(new HelpCommandMessages()));
		messages.setClear(clearTemplate.supply(new ClearCommandMessages()));
		messages.setReload(reloadTemplate.supply(new ReloadCommandMessages()));
		messages.setPlugin(pluginTemplate.supply(new PluginCommandMessages()));
		messages.setStatus(statusTemplate.supply(new StatusCommandMessages()));
		messages.setLanguage(languageTemplate.supply(new LanguageCommandMessages()));
		messages.setUpdateCheck(updateCheckTemplate.supply(new UpdateCheckCommandMessages()));
		messages.setAudit(auditTemplate.supply(new AuditCommandMessages()));

		return messages;
	}
}
