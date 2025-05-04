package me.whereareiam.yui.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Getter
@AllArgsConstructor
public class PayloadButton {
	private final Button button;
	private final String payload;
}
