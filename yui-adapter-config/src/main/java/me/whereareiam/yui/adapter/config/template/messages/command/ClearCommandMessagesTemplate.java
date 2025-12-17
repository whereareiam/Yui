package me.whereareiam.yui.adapter.config.template.messages.command;

import me.whereareiam.yui.model.config.messages.command.ClearCommandMessages;
import me.whereareiam.yui.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClearCommandMessagesTemplate implements DefaultConfig<ClearCommandMessages> {
	@Override
	public ClearCommandMessages getDefault() {
		ClearCommandMessages clearCommand = new ClearCommandMessages();
		clearCommand.setDescription("Clears a user's profile data and reinitializes it. This action cannot be undone.");
		clearCommand.setExample("/yui clear @user");

		ClearCommandMessages.Confirmation confirmation = new ClearCommandMessages.Confirmation();
		confirmation.setTitle("Confirm User Profile Clear");
		confirmation.setDescription(List.of(
				"You are about to clear the profile data for the following user. This action will:",
				"• Remove all cached profile data",
				"• Delete the user's profile from the database",
				"• Create a fresh, empty profile",
				"",
				"**This action cannot be undone!**"
		));
		confirmation.setUserInfo("**Target User:**");
		clearCommand.setConfirmation(confirmation);

		ClearCommandMessages.Success success = new ClearCommandMessages.Success();
		success.setTitle("User Profile Cleared");
		success.setDescription(List.of("The user's profile has been successfully cleared and reinitialized."));
		success.setUserInfo("**Cleared User:**");
		clearCommand.setSuccess(success);

		ClearCommandMessages.Cancelled cancelled = new ClearCommandMessages.Cancelled();
		cancelled.setTitle("Operation Cancelled");
		cancelled.setDescription(List.of("The profile clear operation has been cancelled."));
		clearCommand.setCancelled(cancelled);

		return clearCommand;
	}
}


