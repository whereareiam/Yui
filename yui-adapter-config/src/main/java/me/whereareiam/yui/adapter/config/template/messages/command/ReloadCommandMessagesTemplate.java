package me.whereareiam.yui.adapter.config.template.messages.command;

import me.whereareiam.yui.api.model.config.messages.command.ReloadCommandMessages;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReloadCommandMessagesTemplate implements DefaultConfig<ReloadCommandMessages> {
	@Override
	public ReloadCommandMessages getDefault() {
		ReloadCommandMessages reload = new ReloadCommandMessages();
		reload.setDescription("Reloads all bot components.");
		reload.setExample("/yui reload");

		ReloadCommandMessages.Confirmation confirmation = new ReloadCommandMessages.Confirmation();
		confirmation.setTitle("Confirm System Reload");
		confirmation.setDescription(List.of(
				"You are about to reload the entire system. This action will:",
				"",
				"• Reload all configuration files",
				"• Re-register all Discord commands",
				"• Reload all translation files",
				"• Restart all plugins",
				"• Restart all services",
				"",
				"**This action will temporarily interrupt some bot functionality!**"
		));
		reload.setConfirmation(confirmation);

		ReloadCommandMessages.Success success = new ReloadCommandMessages.Success();
		success.setTitle("System Reloaded Successfully");
		success.setDescription(List.of("All system components have been successfully reloaded with fresh configurations."));
		reload.setSuccess(success);

		ReloadCommandMessages.Cancelled cancelled = new ReloadCommandMessages.Cancelled();
		cancelled.setTitle("Reload Cancelled");
		cancelled.setDescription(List.of("The system reload operation has been cancelled."));
		reload.setCancelled(cancelled);

		ReloadCommandMessages.Error error = new ReloadCommandMessages.Error();
		error.setTitle("Reload Failed");
		error.setDescription(List.of("An error occurred during the system reload. Some components may not have been reloaded properly."));
		reload.setError(error);

		return reload;
	}
}


