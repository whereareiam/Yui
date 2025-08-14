package me.whereareiam.yui.adapter.config.template.messages.command;

import me.whereareiam.yui.api.model.config.messages.command.ReloadCommandMessages;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class ReloadCommandMessagesTemplate implements DefaultConfig<ReloadCommandMessages> {
	@Override
	public ReloadCommandMessages getDefault() {
		ReloadCommandMessages reload = new ReloadCommandMessages();
		reload.setDescription("Reloads all bot components.");
		reload.setExample("/yui reload");

		ReloadCommandMessages.Confirmation confirmation = new ReloadCommandMessages.Confirmation();
		confirmation.setTitle("Confirm System Reload");
		confirmation.setDescription("You are about to reload the entire system. This action will:\n\n• Reload all configuration files\n• Re-register all Discord commands\n• Reload all translation files\n• Restart all plugins\n• Restart all services\n\n**This action will temporarily interrupt some bot functionality!**");
		reload.setConfirmation(confirmation);

		ReloadCommandMessages.Success success = new ReloadCommandMessages.Success();
		success.setTitle("System Reloaded Successfully");
		success.setDescription("All system components have been successfully reloaded with fresh configurations.");
		reload.setSuccess(success);

		ReloadCommandMessages.Cancelled cancelled = new ReloadCommandMessages.Cancelled();
		cancelled.setTitle("Reload Cancelled");
		cancelled.setDescription("The system reload operation has been cancelled.");
		reload.setCancelled(cancelled);

		ReloadCommandMessages.Error error = new ReloadCommandMessages.Error();
		error.setTitle("Reload Failed");
		error.setDescription("An error occurred during the system reload. Some components may not have been reloaded properly.");
		reload.setError(error);

		return reload;
	}
}


