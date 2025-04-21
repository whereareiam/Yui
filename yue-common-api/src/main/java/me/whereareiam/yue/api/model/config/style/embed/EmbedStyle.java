package me.whereareiam.yue.api.model.config.style.embed;

public class EmbedStyle {
	private Embed primary;
	private Embed error;

	public Embed getPrimary() {
		return primary;
	}

	public void setPrimary(Embed primary) {
		this.primary = primary;
	}

	public Embed getError() {
		return error;
	}

	public void setError(Embed error) {
		this.error = error;
	}

	public EmbedStyle() {
	}
}
