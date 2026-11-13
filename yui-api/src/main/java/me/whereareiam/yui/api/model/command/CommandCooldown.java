package me.whereareiam.yui.api.model.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.whereareiam.yui.api.model.type.Duration;
import org.jetbrains.annotations.Nullable;

/**
 * How long a member waits before using a command again. Commands of one {@code group} share their wait; without a
 * group, each command waits on its own.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommandCooldown {
	private boolean enabled;
	private Duration duration;
	private @Nullable String group;
}
