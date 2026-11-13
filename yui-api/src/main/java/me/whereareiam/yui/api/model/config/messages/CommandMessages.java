package me.whereareiam.yui.api.model.config.messages;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.model.config.messages.command.*;

@Getter
@Setter
public class CommandMessages {
	private ErrorMessages error;
	private MainCommandMessages main;
	private HelpCommandMessages help;
	private ClearCommandMessages clear;
	private ReloadCommandMessages reload;
	private PluginCommandMessages plugin;
	private StatusCommandMessages status;
	private LanguageCommandMessages language;
	private UpdateCheckCommandMessages updateCheck;
	private AuditCommandMessages audit;

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
		private CooldownErrorMessages cooldown;
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

		/**
		 * The reply when a command's requirements do not hold, unless the command has its own {@code denied}.
		 */
		@Getter
		@Setter
		public static class RequirementErrorMessages {
			private String title;
			private String description;
		}

		/**
		 * The reply when a member uses a command again before its cooldown ended; {@code {seconds}} is the wait left.
		 */
		@Getter
		@Setter
		public static class CooldownErrorMessages {
			private String title;
			private String description;
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
