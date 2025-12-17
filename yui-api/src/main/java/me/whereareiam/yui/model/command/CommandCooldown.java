package me.whereareiam.yui.model.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommandCooldown {
	private boolean enabled;
	private int cooldown;
	private String group;
}
