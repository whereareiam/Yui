package me.whereareiam.yui.model.fluctlight;

import lombok.Getter;
import me.whereareiam.yui.fluctlight.FluctlightExtension;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fluctlight represents a unified fluctlight model that combines JDA User data
 * with custom application-specific data (languages, roles, etc.).
 * <p>
 * Fluctlight wraps JDA's User interface and delegates all User methods to it,
 * while also providing additional custom data that is persisted in the database.
 * <p>
 * Fluctlight instances are primarily in-memory and are kept in sync with the database.
 * <p>
 * Fluctlight provides convenience methods that update both the in-memory state
 * and persist changes to the database automatically.
 * <p>
 * Fluctlight also supports extensions - modules can attach custom data that is
 * stored in-memory only and not persisted to the database.
 */
@Getter
@SuppressWarnings("unused")
public class Fluctlight {
	private final User jdaUser;

	DiscordLocale primaryLanguage;
	DiscordLocale[] additionalLanguages;
	long[] allowedRoles;
	
	// In-memory extensions - not persisted
	private final Map<String, Object> extensions = new ConcurrentHashMap<>();

	private static FluctlightService managementService;

	/**
	 * Creates a new Fluctlight instance wrapping a JDA User.
	 *
	 * @param jdaUser The JDA User to wrap (must not be null)
	 */
	public Fluctlight(User jdaUser) {
		if (jdaUser == null)
			throw new IllegalArgumentException("JDA User cannot be null");

		this.jdaUser = jdaUser;
		this.additionalLanguages = new DiscordLocale[0];
	}

	/**
	 * Creates a new Fluctlight instance with custom data.
	 *
	 * @param jdaUser            The JDA User to wrap (must not be null)
	 * @param primaryLanguage    The primary language preference
	 * @param additionalLanguages Additional language preferences
	 * @param allowedRoles       Framework role IDs that the bot is allowed to work with
	 *                          (not the fluctlight's guild roles)
	 */
	public Fluctlight(
			User jdaUser,
			DiscordLocale primaryLanguage,
			DiscordLocale[] additionalLanguages,
			long[] allowedRoles
	) {
		if (jdaUser == null)
			throw new IllegalArgumentException("JDA User cannot be null");

		this.jdaUser = jdaUser;
		this.primaryLanguage = primaryLanguage;
		this.additionalLanguages = additionalLanguages != null ? additionalLanguages : new DiscordLocale[0];
		this.allowedRoles = allowedRoles;
	}

	/**
	 * Gets the fluctlight ID from the wrapped JDA User.
	 *
	 * @return The fluctlight's Discord ID
	 */
	public long getId() {
		return jdaUser.getIdLong();
	}

	/**
	 * Gets the username from the wrapped JDA User.
	 *
	 * @return The fluctlight's username
	 */
	public String getName() {
		return jdaUser.getName();
	}

	/**
	 * Gets the effective name from the wrapped JDA User.
	 *
	 * @return The fluctlight's effective name
	 */
	public String getEffectiveName() {
		return jdaUser.getName();
	}

	/**
	 * Gets the mention string for this fluctlight.
	 *
	 * @return The mention string (e.g., "&lt;@123456789>")
	 */
	public String getAsMention() {
		return jdaUser.getAsMention();
	}

	/**
	 * Checks if this Fluctlight has a primary language set.
	 *
	 * @return true if primary language is set, false otherwise
	 */
	public boolean hasPrimaryLanguage() {
		return primaryLanguage != null;
	}

	/**
	 * Checks if this Fluctlight has any additional languages.
	 *
	 * @return true if additional languages exist, false otherwise
	 */
	public boolean hasAdditionalLanguages() {
		return additionalLanguages != null && additionalLanguages.length > 0;
	}

	/**
	 * Checks if this Fluctlight has any allowed roles.
	 * These are framework roles that the bot is allowed to work with,
	 * not the fluctlight's guild roles.
	 *
	 * @return true if allowed roles exist, false otherwise
	 */
	public boolean hasAllowedRoles() {
		return allowedRoles != null && allowedRoles.length > 0;
	}

	/**
	 * Saves this Fluctlight to the database and updates the cache.
	 * This method persists all current state (languages, roles) to the database.
	 */
	public void save() {
		ensureServicesInitialized();
		managementService.save(this);
	}

	/**
	 * Sets the primary language and persists the change to the database.
	 *
	 * @param locale The new primary language locale
	 */
	public void setPrimaryLanguage(DiscordLocale locale) {
		ensureServicesInitialized();
		managementService.updatePrimaryLanguage(this, locale);
	}

	/**
	 * Adds an additional language and persists the change to the database.
	 *
	 * @param locale The additional language locale to add
	 */
	public void addAdditionalLanguage(DiscordLocale locale) {
		ensureServicesInitialized();
		managementService.addAdditionalLanguage(this, locale);
	}

	/**
	 * Removes an additional language and persists the change to the database.
	 *
	 * @param locale The additional language locale to remove
	 */
	public void removeAdditionalLanguage(DiscordLocale locale) {
		ensureServicesInitialized();
		managementService.removeAdditionalLanguage(this, locale);
	}

	/**
	 * Replaces all additional languages and persists the change to the database.
	 *
	 * @param locales The new additional language locales
	 */
	public void setAdditionalLanguages(DiscordLocale[] locales) {
		ensureServicesInitialized();
		managementService.setAdditionalLanguages(this, locales);
	}

	/**
	 * Adds an allowed role and persists the change to the database.
	 * These are framework roles that the bot is allowed to work with,
	 * not the fluctlight's guild roles.
	 *
	 * @param roleId The allowed role ID to add
	 */
	public void addAllowedRole(long roleId) {
		ensureServicesInitialized();
		managementService.addAllowedRole(this, roleId);
	}

	/**
	 * Removes an allowed role and persists the change to the database.
	 * These are framework roles that the bot is allowed to work with,
	 * not the fluctlight's guild roles.
	 *
	 * @param roleId The allowed role ID to remove
	 */
	public void removeAllowedRole(long roleId) {
		ensureServicesInitialized();
		managementService.removeAllowedRole(this, roleId);
	}
	
	/**
	 * Gets extension data by namespace.
	 * <p>
	 * Returns the extension data if it exists, or null if not present.
	 * Extension data is stored in-memory only and is not persisted.
	 *
	 * @param namespace The extension namespace
	 * @param type The expected data type class
	 * @param <T> The data type
	 * @return The extension data, or null if not present
	 */
	public <T> T getExtension(String namespace, Class<T> type) {
		return type.cast(extensions.get(namespace));
	}
	
	/**
	 * Gets extension data or creates it with default values if not present.
	 * <p>
	 * This is the recommended way to access extension data as it ensures
	 * the data is always initialized. Extension data is stored in-memory only.
	 *
	 * @param extension The extension definition
	 * @param <T> The data type
	 * @return The extension data (existing or newly created)
	 */
	public <T> T getOrCreateExtension(FluctlightExtension<T> extension) {
		return extension.getDataType().cast(
			extensions.computeIfAbsent(
				extension.getNamespace(),
				k -> extension.createDefault()
			)
		);
	}
	
	/**
	 * Sets extension data for a namespace.
	 * <p>
	 * If data is null, the extension is removed.
	 * Extension data is stored in-memory only and is not persisted.
	 *
	 * @param namespace The extension namespace
	 * @param data The data to store, or null to remove
	 * @param <T> The data type
	 */
	public <T> void setExtension(String namespace, T data) {
		if (data == null) {
			extensions.remove(namespace);
			return;
		}

		extensions.put(namespace, data);
	}
	
	/**
	 * Checks if extension data exists for a namespace.
	 *
	 * @param namespace The extension namespace
	 * @return true if extension data exists, false otherwise
	 */
	public boolean hasExtension(String namespace) {
		return extensions.containsKey(namespace);
	}
	
	/**
	 * Removes extension data for a namespace.
	 *
	 * @param namespace The extension namespace
	 */
	public void removeExtension(String namespace) {
		extensions.remove(namespace);
	}

	/**
	 * Initializes the static service references.
	 * This is called automatically by Spring via DefaultFluctlightService.
	 *
	 * @param managementService The FluctlightService instance
	 * @param fluctlightPersistence The FluctlightPersistence instance
	 */
	public static void initServices(FluctlightService managementService, FluctlightPersistence fluctlightPersistence) {
		Fluctlight.managementService = managementService;
	}

	/**
	 * Ensures the services are initialized.
	 *
	 * @throws IllegalStateException if the services are not initialized
	 */
	private void ensureServicesInitialized() {
		if (managementService == null)
			throw new IllegalStateException("Fluctlight services not initialized. Ensure Spring context is loaded.");
	}
}
