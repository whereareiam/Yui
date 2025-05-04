package me.whereareiam.yui.api.model.config.style;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class Palette {
	private Color primary;
	private Color secondary;

	private Color success;
	private Color warning;
	private Color error;
	private Color info;
}
