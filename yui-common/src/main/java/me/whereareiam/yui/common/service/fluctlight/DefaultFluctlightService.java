package me.whereareiam.yui.common.service.fluctlight;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.fluctlight.FluctlightClearEvent;
import me.whereareiam.yui.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.event.fluctlight.FluctlightCreatedEvent;
import me.whereareiam.yui.event.fluctlight.FluctlightUpdatedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightAdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightLanguageChangeEvent;
import me.whereareiam.yui.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.event.fluctlight.role.FluctlightRoleAddEvent;
import me.whereareiam.yui.event.fluctlight.role.FluctlightRoleAddedEvent;
import me.whereareiam.yui.event.fluctlight.role.FluctlightRoleRemoveEvent;
import me.whereareiam.yui.event.fluctlight.role.FluctlightRoleRemovedEvent;
import me.whereareiam.yui.fluctlight.FluctlightRegistry;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightData;
import me.whereareiam.yui.model.fluctlight.FluctlightStateUpdater;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of FluctlightService.
 * <p>
 * This service orchestrates operations involving cache, database, and JDA User retrieval.
 * It implements eager loading: when a Fluctlight is first requested, it immediately
 * loads custom data from the database.
 */
@Slf4j
@Service
@AllArgsConstructor
public class DefaultFluctlightService implements FluctlightService {
	private final JDA jda;
	private final FluctlightRegistry fluctlightRegistry;
	private final FluctlightPersistence fluctlightPersistence;
	private final ApplicationEventPublisher eventPublisher;
	private final ObjectProvider<Settings> settings;

	@PostConstruct
	private void initializeFluctlightServices() {
		Fluctlight.initServices(this, fluctlightPersistence);
	}

	@Override
	public Optional<Fluctlight> get(long userId) {
		// First, check registry
		Optional<Fluctlight> cached = fluctlightRegistry.getFluctlight(userId);
		if (cached.isPresent())
			return cached;

		// Get JDA User
		User jdaUser = jda.getUserById(userId);
		if (jdaUser == null) {
			log.debug("User {} not found in JDA cache", userId);
			return Optional.empty();
		}

		// Eager loading: load custom data from database
		Fluctlight fluctlight = new Fluctlight(jdaUser);
		Optional<FluctlightData> customDataOpt = fluctlightPersistence.loadData(fluctlight);

		if (customDataOpt.isPresent()) {
			// Apply custom data from database
			FluctlightData customData = customDataOpt.get();
			FluctlightStateUpdater.updatePrimaryLanguage(fluctlight, customData.getPrimaryLanguage());
			FluctlightStateUpdater.updateAdditionalLanguages(fluctlight, customData.getAdditionalLanguages());
			FluctlightStateUpdater.updateAllowedRoles(fluctlight, customData.getAllowedRoles());
		} else {
			// Create new entry in database with bot's default language
			DiscordLocale defaultLocale = settings.getObject().getLocale();
			FluctlightData initialData = new FluctlightData(defaultLocale, new DiscordLocale[0], null);
			fluctlightPersistence.saveData(fluctlight, initialData);
			log.debug("Created new Fluctlight entry for fluctlight {} with default language {}", userId, defaultLocale);
			
			// Publish creation event - listener will load data and update in-memory state
			eventPublisher.publishEvent(new FluctlightCreatedEvent(fluctlight));
		}

		// Store in registry
		fluctlightRegistry.putFluctlight(userId, fluctlight);
		return Optional.of(fluctlight);
	}

	@Override
	public Fluctlight getOrCreate(long userId) {
		return get(userId)
				.orElseThrow(() -> new IllegalStateException("User " + userId + " not found in JDA"));
	}

	@Override
	public void save(Fluctlight fluctlight) {
		long userId = fluctlight.getId();

		// Update registry
		fluctlightRegistry.putFluctlight(userId, fluctlight);

		// Convert Fluctlight to custom data and save to database
		FluctlightData customData = new FluctlightData(
				fluctlight.getPrimaryLanguage(),
				fluctlight.getAdditionalLanguages(),
				fluctlight.getAllowedRoles()
		);
		fluctlightPersistence.saveData(fluctlight, customData);
		
		// Publish event to synchronize in-memory state (already persisted above)
		eventPublisher.publishEvent(new FluctlightUpdatedEvent(fluctlight, customData));
		log.debug("Saved Fluctlight for fluctlight {}", userId);
	}

	@Override
	public boolean exists(long userId) {
		// Check registry first
		Optional<Fluctlight> cached = fluctlightRegistry.getFluctlight(userId);
		if (cached.isPresent())
			return true;

		// Check database - need to get Fluctlight first to check
		return get(userId).filter(fluctlightPersistence::existsById).isPresent();
	}

	@Override
	public Optional<Fluctlight> clear(long userId) {
		try {
			log.info("Clearing and reinitializing Fluctlight for fluctlight: {}", userId);

			// Get the old Fluctlight before clearing
			Fluctlight oldFluctlight = fluctlightRegistry.getFluctlight(userId).orElse(null);

			// Publish cancellable event before clearing
			FluctlightClearEvent clearEvent = new FluctlightClearEvent(userId, oldFluctlight);
			eventPublisher.publishEvent(clearEvent);

			if (clearEvent.isCancelled()) {
				log.debug("Fluctlight clear operation cancelled for fluctlight: {}", userId);
				return Optional.ofNullable(oldFluctlight);
			}

			// Clear from registry
			fluctlightRegistry.evictFluctlight(userId);
			log.debug("Evicted fluctlight {} from registry", userId);

			// Reset data in database with bot's default language
			User jdaUser = jda.getUserById(userId);
			if (jdaUser != null) {
				Fluctlight tempFluctlight = new Fluctlight(jdaUser);
				DiscordLocale defaultLocale = settings.getObject().getLocale();
				FluctlightData resetData = new FluctlightData(defaultLocale, new DiscordLocale[0], null);
				fluctlightPersistence.saveData(tempFluctlight, resetData);
			}
			log.debug("Reset fluctlight {} data in database", userId);

			// Create fresh Fluctlight (will load reset data from DB and register in-memory)
			Optional<Fluctlight> newFluctlight = get(userId);
			if (newFluctlight.isPresent()) {
				// Publish non-cancellable event after clearing is complete
				FluctlightClearedEvent clearedEvent = new FluctlightClearedEvent(
						userId, oldFluctlight, newFluctlight.get()
				);
				eventPublisher.publishEvent(clearedEvent);
				
				log.debug("Successfully reinitialized Fluctlight for fluctlight: {}", userId);
				return newFluctlight;
			}

			log.error("Failed to create new Fluctlight for fluctlight: {}", userId);
			return Optional.empty();
		} catch (Exception e) {
			log.error("Error during Fluctlight clear and reinitialization for fluctlight: {}", userId, e);
			return Optional.empty();
		}
	}

	@Override
	public void updatePrimaryLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		DiscordLocale oldLanguage = fluctlight.getPrimaryLanguage();
		
		// Publish cancellable event BEFORE persistence
		FluctlightLanguageChangeEvent event = new FluctlightLanguageChangeEvent(fluctlight, oldLanguage);
		eventPublisher.publishEvent(event);
		
		if (event.isCancelled()) {
			log.debug("Primary language change cancelled for user {}", fluctlight.getId());
			return;
		}
		
		// Update persistence
		fluctlightPersistence.updatePrimaryLanguage(fluctlight, locale);
		
		// Publish non-cancellable event AFTER persistence
		eventPublisher.publishEvent(new FluctlightLanguageChangedEvent(fluctlight, oldLanguage, locale));
		
		// Publish generic update event
		FluctlightData updatedData = new FluctlightData(
				locale,
				fluctlight.getAdditionalLanguages(),
				fluctlight.getAllowedRoles()
		);
		eventPublisher.publishEvent(new FluctlightUpdatedEvent(fluctlight, updatedData));
		
		// Update registry
		fluctlightRegistry.putFluctlight(fluctlight.getId(), fluctlight);
	}

	@Override
	public void addAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		// Publish cancellable event BEFORE persistence
		FluctlightAdditionalLanguageAddedEvent event = new FluctlightAdditionalLanguageAddedEvent(fluctlight);
		event.setLanguage(locale);
		eventPublisher.publishEvent(event);
		
		if (event.isCancelled()) {
			log.debug("Additional language add cancelled for user {} (language: {})", fluctlight.getId(), locale);
			return;
		}
		
		// Update persistence
		fluctlightPersistence.addAdditionalLanguage(fluctlight, locale);
		
		// Publish generic update event
		DiscordLocale[] current = fluctlight.getAdditionalLanguages();
		DiscordLocale[] updated = new DiscordLocale[current.length + 1];
		System.arraycopy(current, 0, updated, 0, current.length);
		updated[current.length] = locale;
		FluctlightData updatedData = new FluctlightData(
				fluctlight.getPrimaryLanguage(),
				updated,
				fluctlight.getAllowedRoles()
		);
		eventPublisher.publishEvent(new FluctlightUpdatedEvent(fluctlight, updatedData));
		
		// Update registry
		fluctlightRegistry.putFluctlight(fluctlight.getId(), fluctlight);
	}

	@Override
	public void removeAdditionalLanguage(Fluctlight fluctlight, DiscordLocale locale) {
		// Publish cancellable event BEFORE persistence
		FluctlightAdditionalLanguageRemovedEvent event = new FluctlightAdditionalLanguageRemovedEvent(fluctlight);
		event.setLanguage(locale);
		eventPublisher.publishEvent(event);
		
		if (event.isCancelled()) {
			log.debug("Additional language remove cancelled for user {} (language: {})", fluctlight.getId(), locale);
			return;
		}
		
		// Update persistence
		fluctlightPersistence.removeAdditionalLanguage(fluctlight, locale);
		
		// Publish generic update event
		DiscordLocale[] current = fluctlight.getAdditionalLanguages();
		DiscordLocale[] updated = new DiscordLocale[Math.max(0, current.length - 1)];
		int index = 0;
		for (DiscordLocale lang : current)
			if (lang != locale)
				updated[index++] = lang;

		FluctlightData updatedData = new FluctlightData(
				fluctlight.getPrimaryLanguage(),
				updated,
				fluctlight.getAllowedRoles()
		);
		eventPublisher.publishEvent(new FluctlightUpdatedEvent(fluctlight, updatedData));
		
		// Update registry
		fluctlightRegistry.putFluctlight(fluctlight.getId(), fluctlight);
	}

	@Override
	public void setAdditionalLanguages(Fluctlight fluctlight, DiscordLocale[] locales) {
		Set<DiscordLocale> current = toLocaleSet(fluctlight.getAdditionalLanguages());
		Set<DiscordLocale> desired = toLocaleSet(locales);

		Set<DiscordLocale> toRemove = new HashSet<>(current);
		toRemove.removeAll(desired);

		Set<DiscordLocale> toAdd = new HashSet<>(desired);
		toAdd.removeAll(current);

		Set<DiscordLocale> updatedSet = new HashSet<>(current);

		for (DiscordLocale locale : toRemove) {
			FluctlightAdditionalLanguageRemovedEvent event = new FluctlightAdditionalLanguageRemovedEvent(fluctlight);
			event.setLanguage(locale);
			eventPublisher.publishEvent(event);
			if (!event.isCancelled())
				updatedSet.remove(locale);
		}

		for (DiscordLocale locale : toAdd) {
			FluctlightAdditionalLanguageAddedEvent event = new FluctlightAdditionalLanguageAddedEvent(fluctlight);
			event.setLanguage(locale);
			eventPublisher.publishEvent(event);
			if (!event.isCancelled())
				updatedSet.add(locale);
		}

		DiscordLocale[] updated = updatedSet.toArray(new DiscordLocale[0]);
		fluctlightPersistence.updateAdditionalLanguages(fluctlight, updated);

		FluctlightData updatedData = new FluctlightData(
				fluctlight.getPrimaryLanguage(),
				updated,
				fluctlight.getAllowedRoles()
		);
		eventPublisher.publishEvent(new FluctlightUpdatedEvent(fluctlight, updatedData));

		fluctlightRegistry.putFluctlight(fluctlight.getId(), fluctlight);
	}

	@Override
	public void addAllowedRole(Fluctlight fluctlight, long roleId) {
		// Publish cancellable event BEFORE persistence
		FluctlightRoleAddEvent addEvent = new FluctlightRoleAddEvent(fluctlight, roleId);
		eventPublisher.publishEvent(addEvent);
		
		if (addEvent.isCancelled()) {
			log.debug("Role add operation cancelled for user {} (roleId: {})", fluctlight.getId(), roleId);
			return;
		}
		
		// Update persistence
		fluctlightPersistence.addAllowedRole(fluctlight, roleId);
		
		// Publish non-cancellable event AFTER persistence
		eventPublisher.publishEvent(new FluctlightRoleAddedEvent(fluctlight, roleId));
		
		// Publish generic update event for consistency with language changes
		long[] current = fluctlight.getAllowedRoles();
		long[] updated;
		if (current == null) {
			updated = new long[]{roleId};
		} else {
			updated = new long[current.length + 1];
			System.arraycopy(current, 0, updated, 0, current.length);
			updated[current.length] = roleId;
		}
		
		FluctlightData updatedData = new FluctlightData(
				fluctlight.getPrimaryLanguage(),
				fluctlight.getAdditionalLanguages(),
				updated
		);
		eventPublisher.publishEvent(new FluctlightUpdatedEvent(fluctlight, updatedData));
	}

	@Override
	public void removeAllowedRole(Fluctlight fluctlight, long roleId) {
		// Publish cancellable event BEFORE persistence
		FluctlightRoleRemoveEvent removeEvent = new FluctlightRoleRemoveEvent(fluctlight, roleId);
		eventPublisher.publishEvent(removeEvent);
		
		if (removeEvent.isCancelled()) {
			log.debug("Role remove operation cancelled for user {} (roleId: {})", fluctlight.getId(), roleId);
			return;
		}
		
		// Update persistence
		fluctlightPersistence.removeAllowedRole(fluctlight, roleId);
		
		// Publish non-cancellable event AFTER persistence
		eventPublisher.publishEvent(new FluctlightRoleRemovedEvent(fluctlight, roleId));
		
		// Publish generic update event for consistency with language changes
		long[] current = fluctlight.getAllowedRoles();
		if (current == null || current.length == 0)
			return;
		
		long[] updated = new long[current.length - 1];
		int index = 0;
		for (long role : current)
			if (role != roleId) updated[index++] = role;

		FluctlightData updatedData = new FluctlightData(
				fluctlight.getPrimaryLanguage(),
				fluctlight.getAdditionalLanguages(),
				updated.length > 0 ? updated : null
		);
		eventPublisher.publishEvent(new FluctlightUpdatedEvent(fluctlight, updatedData));
	}

	private Set<DiscordLocale> toLocaleSet(DiscordLocale[] locales) {
		Set<DiscordLocale> result = new HashSet<>();
		if (locales == null)
			return result;
		for (DiscordLocale locale : locales) {
			if (locale != null)
				result.add(locale);
		}
		return result;
	}

}