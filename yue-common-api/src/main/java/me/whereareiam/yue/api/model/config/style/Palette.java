package me.whereareiam.yue.api.model.config.style;

import java.awt.*;

public class Palette {
	private Color primary;
	private Color secondary;

	private Color error;
	private Color warning;
	private Color success;
	private Color info;

	public Color getPrimary() {
		return primary;
	}

	public void setPrimary(Color primary) {
		this.primary = primary;
	}

	public Color getSecondary() {
		return secondary;
	}

	public void setSecondary(Color secondary) {
		this.secondary = secondary;
	}

	public Color getError() {
		return error;
	}

	public void setError(Color error) {
		this.error = error;
	}

	public Color getWarning() {
		return warning;
	}

	public void setWarning(Color warning) {
		this.warning = warning;
	}

	public Color getSuccess() {
		return success;
	}

	public void setSuccess(Color success) {
		this.success = success;
	}

	public Color getInfo() {
		return info;
	}

	public void setInfo(Color info) {
		this.info = info;
	}
}
