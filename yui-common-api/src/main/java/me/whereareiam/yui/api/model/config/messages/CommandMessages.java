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
	private LanguageCommandMessages language;

	@Getter
	@Setter
	public static class ErrorMessages {
		private String exception;
		private RequirementErrorMessages requirement;
		private ValidationErrorMessages validation;

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
