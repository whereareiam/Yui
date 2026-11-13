package me.whereareiam.yui.api.model.config.settings;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.api.type.update.UpdateCheck;
import me.whereareiam.yui.api.type.update.UpdateWarning;

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
