package me.whereareiam.yui.model.config.messages;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.config.messages.command.*;

@Getter
@Setter
public class CommandMessages {
	private ErrorMessages error;
	private MainCommandMessages main;
	private HelpCommandMessages help;
	private ClearCommandMessages clear;
	private ReloadCommandMessages reload;
	private PluginCommandMessages plugin;
	private LanguageCommandMessages language;

	@Getter
	@Setter
	public static class ErrorMessages {
		private String exception;
		private CommandErrorMessages command;
		private SyntaxErrorMessages syntax;
		private ArgumentErrorMessages argument;
		private PermissionErrorMessages permission;
		private SenderErrorMessages sender;
		private UnexpectedErrorMessages unexpected;
		private RequirementErrorMessages requirement;
		private ValidationErrorMessages validation;

		@Getter
		@Setter
		public static class CommandErrorMessages {
			private String title;
			private String description;
		}

		@Getter
		@Setter
		public static class SyntaxErrorMessages {
			private String title;
			private String description;
			private Fields fields;

			@Getter
			@Setter
			public static class Fields {
				private String correctUsage;
			}
		}

		@Getter
		@Setter
		public static class ArgumentErrorMessages {
			private String title;
			private String description;
			private Fields fields;

			@Getter
			@Setter
			public static class Fields {
				private String errorDetails;
			}
		}

		@Getter
		@Setter
		public static class PermissionErrorMessages {
			private String title;
			private String description;
			private Fields fields;

			@Getter
			@Setter
			public static class Fields {
				private String requiredPermission;
			}
		}

		@Getter
		@Setter
		public static class SenderErrorMessages {
			private String title;
			private String description;
		}

		@Getter
		@Setter
		public static class UnexpectedErrorMessages {
			private String title;
			private String description;
		}

		@Getter
		@Setter
		public static class RequirementErrorMessages {
			private String title;
			private String unknown;
			private String failed;

			private String role;
			private String roleUnknown;
			private String scope;
			private String scopeUnknown;
			private String channel;
			private String channelUnknown;
			private String user;
			private String userUnknown;
			private String guild;
			private String guildUnknown;
		}

		@Getter
		@Setter
		public static class ValidationErrorMessages {
			private String sameUser;
			private String userRequired;
			private String invalidButton;
		}
	}
}
