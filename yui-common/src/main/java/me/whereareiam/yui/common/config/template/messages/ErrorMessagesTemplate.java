package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.messages.CommandMessages;
import me.whereareiam.yui.model.config.messages.CommandMessages.ErrorMessages;
import me.whereareiam.yui.model.config.messages.CommandMessages.ErrorMessages.RequirementErrorMessages;
import me.whereareiam.yui.model.config.messages.CommandMessages.ErrorMessages.ValidationErrorMessages;
import org.springframework.stereotype.Component;

@Component
public class ErrorMessagesTemplate implements TemplateProvider<ErrorMessages> {
	@Override
	public ErrorMessages supply(ErrorMessages error) {
		error.setException("An unexpected error occurred. Please try again later.");

		CommandMessages.ErrorMessages.CommandErrorMessages command = new CommandMessages.ErrorMessages.CommandErrorMessages();
		command.setTitle("Command not found");
		command.setDescription("The command you tried to use does not exist. Please check the command name and try again.");
		error.setCommand(command);

		CommandMessages.ErrorMessages.SyntaxErrorMessages syntax = new CommandMessages.ErrorMessages.SyntaxErrorMessages();
		syntax.setTitle("Invalid syntax");
		syntax.setDescription("The command syntax is incorrect.");
		CommandMessages.ErrorMessages.SyntaxErrorMessages.Fields syntaxFields = new CommandMessages.ErrorMessages.SyntaxErrorMessages.Fields();
		syntaxFields.setCorrectUsage("Correct usage");
		syntax.setFields(syntaxFields);
		error.setSyntax(syntax);

		CommandMessages.ErrorMessages.ArgumentErrorMessages argument = new CommandMessages.ErrorMessages.ArgumentErrorMessages();
		argument.setTitle("Invalid argument");
		argument.setDescription("One of the arguments you provided could not be parsed.");
		CommandMessages.ErrorMessages.ArgumentErrorMessages.Fields argumentFields = new CommandMessages.ErrorMessages.ArgumentErrorMessages.Fields();
		argumentFields.setErrorDetails("Error details");
		argument.setFields(argumentFields);
		error.setArgument(argument);

		CommandMessages.ErrorMessages.PermissionErrorMessages permission = new CommandMessages.ErrorMessages.PermissionErrorMessages();
		permission.setTitle("Permission denied");
		permission.setDescription("You do not have permission to use this command.");
		CommandMessages.ErrorMessages.PermissionErrorMessages.Fields permissionFields = new CommandMessages.ErrorMessages.PermissionErrorMessages.Fields();
		permissionFields.setRequiredPermission("Required permission");
		permission.setFields(permissionFields);
		error.setPermission(permission);

		CommandMessages.ErrorMessages.SenderErrorMessages sender = new CommandMessages.ErrorMessages.SenderErrorMessages();
		sender.setTitle("Invalid command sender");
		sender.setDescription("This command cannot be used in this context.");
		error.setSender(sender);

		CommandMessages.ErrorMessages.UnexpectedErrorMessages unexpected = new CommandMessages.ErrorMessages.UnexpectedErrorMessages();
		unexpected.setTitle("Unexpected error");
		unexpected.setDescription("An unexpected error occurred while processing your command:\n```\n<p:value>\n```");
		error.setUnexpected(unexpected);

		RequirementErrorMessages requirement = new RequirementErrorMessages();
		requirement.setTitle("Seems like you don't have the rights");
		requirement.setUnknown("You do not meet the requirements for this command. We could not determine the exact reason, if you think this is an error, please contact the server administrator.");
		requirement.setFailed("You do not meet the following requirements:\n\n<p:requirements>");
		requirement.setRole("**Required Role(s):**\n <p:roles>");
		requirement.setRoleUnknown("**Required Role(s):**\n *Unknown*");
		requirement.setScope("**Required Scope(s):**\n <p:scopes>");
		requirement.setScopeUnknown("**Required Scope(s):**\n *Unknown*");
		requirement.setChannel("**Required Channel Type(s):**\n <p:channelTypes>");
		requirement.setChannelUnknown("**Required Channel Type(s):**\n *Unknown*");
		requirement.setUser("**User Restriction:**\n <p:userIds>");
		requirement.setUserUnknown("**User Restriction:**\n *Unknown*");
		requirement.setGuild("**Guild Restriction:**\n <p:guildIds>");
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
