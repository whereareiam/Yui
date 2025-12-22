package me.whereareiam.yui.model.config.messages;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeneralMessages {
	private Conversation conversation;

	@Getter
	@Setter
	public static class Conversation {
		private Close close;

		@Getter
		@Setter
		public static class Close {
			private String title;
			private List<String> description;
		}
	}
}
