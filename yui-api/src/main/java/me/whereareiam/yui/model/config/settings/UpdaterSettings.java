package me.whereareiam.yui.model.config.settings;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.model.type.Duration;
import me.whereareiam.yui.type.update.UpdateCheck;
import me.whereareiam.yui.type.update.UpdateWarning;

import java.util.Set;

/**
 * How often the updater looks for new versions, of what, and what it warns about.
 */
@Getter
@Setter
public class UpdaterSettings {
	private Duration interval;
	private Set<UpdateCheck> check;
	private Set<UpdateWarning> warn;
}
