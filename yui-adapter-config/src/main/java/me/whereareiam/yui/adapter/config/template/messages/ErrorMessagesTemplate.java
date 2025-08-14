package me.whereareiam.yui.adapter.config.template.messages;

import me.whereareiam.yui.api.model.config.messages.CommandMessages.ErrorMessages;
import me.whereareiam.yui.api.model.config.messages.CommandMessages.ErrorMessages.RequirementErrorMessages;
import me.whereareiam.yui.api.model.config.messages.CommandMessages.ErrorMessages.ValidationErrorMessages;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class ErrorMessagesTemplate implements DefaultConfig<ErrorMessages> {
	@Override
	public ErrorMessages getDefault() {
		ErrorMessages error = new ErrorMessages();
		error.setException("An unexpected error occurred. Please try again later.");

		RequirementErrorMessages requirement = new RequirementErrorMessages();
		requirement.setTitle("Seems like you don't have the rights");
		requirement.setUnknown("You do not meet the requirements for this command. We could not determine the exact reason, if you think this is an error, please contact the server administrator.");
		requirement.setFailed("You do not meet the following requirements:\n\n{0}");
		requirement.setRole("**Required Role(s):**\n {0}");
		requirement.setRoleUnknown("**Required Role(s):**\n *Unknown*");
		requirement.setScope("**Required Scope(s):**\n {0}");
		requirement.setScopeUnknown("**Required Scope(s):**\n *Unknown*");
		requirement.setChannel("**Required Channel Type(s):**\n {0}");
		requirement.setChannelUnknown("**Required Channel Type(s):**\n *Unknown*");
		requirement.setUser("**User Restriction:**\n {0}");
		requirement.setUserUnknown("**User Restriction:**\n *Unknown*");
		requirement.setGuild("**Guild Restriction:**\n {0}");
		requirement.setGuildUnknown("**Guild Restriction:**\n *Unknown*");
		error.setRequirement(requirement);

		ValidationErrorMessages validation = new ValidationErrorMessages();
		validation.setSameUser("You cannot use this command on yourself!");
		validation.setUserRequired("User parameter is required!");
		validation.setInvalidButton("Invalid button configuration!");
		error.setValidation(validation);

		return error;
	}
}


