package me.whereareiam.yue.api.model.config.style.embed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Embed {
	private Author author;
	private Footer footer;
	private String titleUrl;
	private String thumbnailUrl;
	private String imageUrl;
	private boolean timestamp;

	@Setter
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Author {
		private String name;
		private String iconUrl;
		private String url;
	}

	@Setter
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Footer {
		private String text;
		private String iconUrl;
	}

}
