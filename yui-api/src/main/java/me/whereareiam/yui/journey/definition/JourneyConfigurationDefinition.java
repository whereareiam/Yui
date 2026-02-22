package me.whereareiam.yui.journey.definition;

import org.jetbrains.annotations.Nullable;

/**
 * Optional journey-level configuration contract.
 */
public interface JourneyConfigurationDefinition {
	/**
	 * Provides preferred session store id for this journey.
	 *
	 * @return store id or {@code null} to use default resolution
	 */
	default @Nullable String sessionStore() {
		return null;
	}
}
