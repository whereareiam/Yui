package me.whereareiam.yue.api.model.config.style;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class Palette {
	private Color primary;
	private Color secondary;

	private Color error;
	private Color warning;
	private Color success;
	private Color info;
}
