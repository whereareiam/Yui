package me.whereareiam.yui.model.fluctlight;

import net.dv8tion.jda.api.interactions.DiscordLocale;

/**
 * <b>INTERNAL API - DO NOT USE IN APPLICATION CODE</b>
 * <p>
 * This class is part of Yui's internal framework infrastructure and is used exclusively
 * by event listeners to synchronize in-memory Fluctlight state after database persistence.
 * <p>
 * <b>WARNING:</b> Using this class directly in application code will:
 * <ul>
 *   <li>Bypass database persistence (changes lost on restart)</li>
 *   <li>Skip event publishing (listeners won't be notified)</li>
 *   <li>Prevent role synchronization (Discord roles won't update)</li>
 *   <li>Create inconsistent state between memory and database</li>
 * </ul>
 * <p>
 * <b>Correct usage:</b> Always use FluctlightService or Fluctlight's public methods
 * (e.g., {@code fluctlight.addAllowedRole(roleId)} or {@code fluctlight.setPrimaryLanguage(locale)}).
 * <p>
 * This class must remain public due to Java module visibility constraints, but it is
 * not part of the public API contract and may change without notice.
 *
 * @see me.whereareiam.yui.fluctlight.FluctlightService
 */
public final class FluctlightStateUpdater {
	
	/**
	 * <b>INTERNAL USE ONLY</b> - Updates primary language in-memory without persisting.
	 * <p>
	 * This method is called by framework listeners after database persistence.
	 * Application code must use {@code fluctlight.setPrimaryLanguage(locale)} instead.
	 * 
	 * @param fluctlight The Fluctlight instance to update
	 * @param locale The new primary language locale
	 */
	public static void updatePrimaryLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		fluctlight.primaryLanguage = locale;
	}
	
	/**
	 * <b>INTERNAL USE ONLY</b> - Updates additional languages in-memory without persisting.
	 * <p>
	 * This method is called by framework listeners after database persistence.
	 * Application code must use {@code fluctlight.addAdditionalLanguage(locale)} instead.
	 * 
	 * @param fluctlight The Fluctlight instance to update
	 * @param locales The additional language locales
	 */
	public static void updateAdditionalLanguages(Fluctlight fluctlight, DiscordLocale[] locales) {
		fluctlight.additionalLanguages = locales != null ? locales : new DiscordLocale[0];
	}
	
	/**
	 * <b>INTERNAL USE ONLY</b> - Updates allowed roles in-memory without persisting.
	 * <p>
	 * This method is called by framework listeners after database persistence.
	 * Application code must use {@code fluctlight.addAllowedRole(roleId)} instead.
	 * 
	 * @param fluctlight The Fluctlight instance to update
	 * @param roleIds The allowed role IDs
	 */
	public static void updateAllowedRoles(Fluctlight fluctlight, long[] roleIds) {
		fluctlight.allowedRoles = roleIds;
	}
}
