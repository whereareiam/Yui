package me.whereareiam.yui.model.config.messages;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AuditMessages {
	private User user;

	@Getter
	@Setter
	public static class User {
		private Join join;
		private Leave leave;
		private Kick kick;

		@Getter
		@Setter
		public static class Join {
			private String title;
			private List<String> description;
			private Fields fields;

			@Getter
			@Setter
			public static class Fields {
				private String target;
				private String accountCreated;
			}
		}

		@Getter
		@Setter
		public static class Leave {
			private String title;
			private List<String> description;
			private Fields fields;

			@Getter
			@Setter
			public static class Fields {
				private String target;
			}
		}

		@Getter
		@Setter
		public static class Kick {
			private String title;
			private List<String> description;
			private Fields fields;

			@Getter
			@Setter
			public static class Fields {
				private String target;
				private String reason;
				private String moderator;
			}
		}
	}
}
