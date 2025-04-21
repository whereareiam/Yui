package me.whereareiam.yue.api.model.config.style.embed;

public class Embed {
	private Author author;
	private Footer footer;
	private String titleUrl;
	private String thumbnailUrl;
	private String imageUrl;
	private boolean timestamp;

	public Embed() {
	}

	public Embed(Author author, Footer footer, String titleUrl, String thumbnailUrl, String imageUrl, boolean timestamp) {
		this.author = author;
		this.footer = footer;
		this.titleUrl = titleUrl;
		this.thumbnailUrl = thumbnailUrl;
		this.imageUrl = imageUrl;
		this.timestamp = timestamp;
	}

	public static class Author {
		private String name;
		private String iconUrl;
		private String url;

		public Author() {
		}

		public Author(String name, String iconUrl, String url) {
			this.name = name;
			this.iconUrl = iconUrl;
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getIconUrl() {
			return iconUrl;
		}

		public void setIconUrl(String iconUrl) {
			this.iconUrl = iconUrl;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}

	public static class Footer {
		private String text;
		private String iconUrl;

		public Footer() {
		}

		public Footer(String text, String iconUrl) {
			this.text = text;
			this.iconUrl = iconUrl;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getIconUrl() {
			return iconUrl;
		}

		public void setIconUrl(String iconUrl) {
			this.iconUrl = iconUrl;
		}
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public Footer getFooter() {
		return footer;
	}

	public void setFooter(Footer footer) {
		this.footer = footer;
	}

	public String getTitleUrl() {
		return titleUrl;
	}

	public void setTitleUrl(String titleUrl) {
		this.titleUrl = titleUrl;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public boolean isTimestamp() {
		return timestamp;
	}

	public void setTimestamp(boolean timestamp) {
		this.timestamp = timestamp;
	}
}
