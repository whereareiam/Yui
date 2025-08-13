package me.whereareiam.yui.api.model.config.messages;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.model.config.messages.command.HelpCommandMessages;
import me.whereareiam.yui.api.model.config.messages.command.MainCommandMessages;

@Getter
@Setter
public class CommandMessages {
	private ErrorMessages error;
	private MainCommandMessages main;
	private HelpCommandMessages help;

	@Getter
	@Setter
	public static class ErrorMessages {
		private String exception;
		private RequirementErrorMessages requirement;

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
	}
}
