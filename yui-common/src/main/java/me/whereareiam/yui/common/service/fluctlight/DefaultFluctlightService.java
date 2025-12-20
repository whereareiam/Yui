package me.whereareiam.yui.common.service.fluctlight;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.event.fluctlight.FluctlightCreatedEvent;
import me.whereareiam.yui.fluctlight.FluctlightRegistry;
import me.whereareiam.yui.fluctlight.FluctlightService;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightData;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
		Optional<FluctlightData> customDataOpt = fluctlightPersistence.loadData(userId);

		if (customDataOpt.isPresent()) {
			// Apply custom data from database
			FluctlightData customData = customDataOpt.get();
			fluctlight.setPrimaryLanguage(customData.getPrimaryLanguage());
			fluctlight.setAdditionalLanguages(customData.getAdditionalLanguages());
			fluctlight.setAllowedRoles(customData.getAllowedRoles());
		} else {
			// Create new entry in database with empty custom data
			FluctlightData emptyData = new FluctlightData(null, new DiscordLocale[0], null);
			fluctlightPersistence.saveData(userId, emptyData);
			log.debug("Created new Fluctlight entry for fluctlight {}", userId);
			
			// Publish creation event
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
		fluctlightPersistence.saveData(userId, customData);
		log.debug("Saved Fluctlight for fluctlight {}", userId);
	}

	@Override
	public boolean exists(long userId) {
		// Check registry first
		if (fluctlightRegistry.getFluctlight(userId).isPresent())
			return true;

		// Check database
		return fluctlightPersistence.loadData(userId).isPresent();
	}

	@Override
	public Optional<Fluctlight> clear(long userId) {
		try {
			log.info("Clearing and reinitializing Fluctlight for fluctlight: {}", userId);

			// Get the old Fluctlight before clearing
			Fluctlight oldFluctlight = fluctlightRegistry.getFluctlight(userId).orElse(null);

			// Publish event before clearing
			FluctlightClearedEvent event = new FluctlightClearedEvent(userId, oldFluctlight, null);
			eventPublisher.publishEvent(event);

			if (event.isCancelled()) {
				log.debug("Fluctlight clear operation cancelled for fluctlight: {}", userId);
				return Optional.ofNullable(oldFluctlight);
			}

			// Clear from registry
			fluctlightRegistry.evictFluctlight(userId);
			log.debug("Evicted fluctlight {} from registry", userId);

			// Delete from database - create empty custom data to reset
			FluctlightData emptyData = new FluctlightData(null, new DiscordLocale[0], null);
			fluctlightPersistence.saveData(userId, emptyData);
			log.debug("Deleted fluctlight {} Fluctlight from database", userId);

			// Create fresh Fluctlight
			Optional<Fluctlight> newFluctlight = get(userId);
			if (newFluctlight.isPresent()) {
				// Update event with new Fluctlight and republish
				FluctlightClearedEvent completedEvent = new FluctlightClearedEvent(
						userId, oldFluctlight, newFluctlight.get()
				);
				eventPublisher.publishEvent(completedEvent);
				
				log.info("Successfully reinitialized Fluctlight for fluctlight: {}", userId);
				return newFluctlight;
			}

			log.error("Failed to create new Fluctlight for fluctlight: {}", userId);
			return Optional.empty();
		} catch (Exception e) {
			log.error("Error during Fluctlight clear and reinitialization for fluctlight: {}", userId, e);
			return Optional.empty();
		}
	}

}