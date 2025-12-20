package me.whereareiam.yui.adapter.database.adapter;

import me.whereareiam.yui.adapter.database.entity.FluctlightEntity;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.entity.RoleEntity;
import me.whereareiam.yui.adapter.database.mapper.FluctlightMapper;
import me.whereareiam.yui.adapter.database.repository.FluctlightRepository;
import me.whereareiam.yui.adapter.database.repository.LanguageRepository;
import me.whereareiam.yui.adapter.database.repository.RoleRepository;
import me.whereareiam.yui.event.language.AdditionalLanguageAddedEvent;
import me.whereareiam.yui.event.language.AdditionalLanguageRemovedEvent;
import me.whereareiam.yui.event.language.LanguageChangeEvent;
import me.whereareiam.yui.model.fluctlight.FluctlightData;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for FluctlightPersistenceAdapter using Testcontainers.
 * Tests database operations with a real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
class FluctlightPersistenceAdapterTest {
	@Container
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.1")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private FluctlightRepository fluctlightRepository;

	@Autowired
	private LanguageRepository languageRepository;

	@Autowired
	private RoleRepository roleRepository;

	private ApplicationEventPublisher eventPublisher;
	private FluctlightPersistenceAdapter adapter;

	private LanguageEntity englishLanguage;
	private LanguageEntity germanLanguage;
	private LanguageEntity frenchLanguage;
	private RoleEntity role1;
	private RoleEntity role2;

	@BeforeEach
	void setUp() {
		eventPublisher = mock(ApplicationEventPublisher.class);
		adapter = new FluctlightPersistenceAdapter(
				fluctlightRepository,
				languageRepository,
				roleRepository,
				new FluctlightMapper(),
				eventPublisher
		);

		// Create test languages
		englishLanguage = LanguageEntity.builder()
				.locale(DiscordLocale.ENGLISH_US)
				.build();
		germanLanguage = LanguageEntity.builder()
				.locale(DiscordLocale.GERMAN)
				.build();
		frenchLanguage = LanguageEntity.builder()
				.locale(DiscordLocale.FRENCH)
				.build();
		entityManager.persist(englishLanguage);
		entityManager.persist(germanLanguage);
		entityManager.persist(frenchLanguage);
		entityManager.flush();

		// Create test roles
		role1 = RoleEntity.builder().id(100L).build();
		role2 = RoleEntity.builder().id(200L).build();
		entityManager.persist(role1);
		entityManager.persist(role2);
		entityManager.flush();
	}

	// Basic CRUD operations

	@Test
	void loadData_WhenEntityDoesNotExist_ReturnsEmpty() {
		// Act
		Optional<FluctlightData> result = adapter.loadData(999L);

		// Assert
		assertFalse(result.isPresent());
	}

	@Test
	void loadData_WhenEntityExists_ReturnsData() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.primaryLanguage(englishLanguage)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entity.getAdditionalLanguages().add(germanLanguage);
		entity.getRoles().add(role1);
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		Optional<FluctlightData> result = adapter.loadData(123L);

		// Assert
		assertTrue(result.isPresent());
		FluctlightData data = result.get();
		assertEquals(DiscordLocale.ENGLISH_US, data.getPrimaryLanguage());
		assertEquals(1, data.getAdditionalLanguages().length);
		assertEquals(DiscordLocale.GERMAN, data.getAdditionalLanguages()[0]);
		assertNotNull(data.getAllowedRoles());
		assertEquals(1, data.getAllowedRoles().length);
		assertEquals(100L, data.getAllowedRoles()[0]);
	}

	@Test
	void saveData_WhenEntityDoesNotExist_CreatesNew() {
		// Arrange
		FluctlightData data = new FluctlightData(
				DiscordLocale.ENGLISH_US,
				new DiscordLocale[]{DiscordLocale.GERMAN},
				new long[]{100L}
		);

		// Act
		adapter.saveData(123L, data);

		// Assert
		Optional<FluctlightEntity> entity = fluctlightRepository.findById(123L);
		assertTrue(entity.isPresent());
		assertEquals(englishLanguage.getId(), entity.get().getPrimaryLanguage().getId());
		assertEquals(1, entity.get().getAdditionalLanguages().size());
		assertEquals(1, entity.get().getRoles().size());
	}

	@Test
	void saveData_WhenEntityExists_UpdatesExisting() {
		// Arrange
		FluctlightEntity existing = FluctlightEntity.builder()
				.id(123L)
				.primaryLanguage(englishLanguage)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(existing);
		entityManager.flush();
		entityManager.clear();

		FluctlightData newData = new FluctlightData(
				DiscordLocale.GERMAN,
				new DiscordLocale[]{DiscordLocale.FRENCH},
				new long[]{200L}
		);

		// Act
		adapter.saveData(123L, newData);

		// Assert
		Optional<FluctlightEntity> entity = fluctlightRepository.findById(123L);
		assertTrue(entity.isPresent());
		assertEquals(germanLanguage.getId(), entity.get().getPrimaryLanguage().getId());
		assertEquals(1, entity.get().getAdditionalLanguages().size());
		assertTrue(entity.get().getAdditionalLanguages().contains(frenchLanguage));
		assertEquals(1, entity.get().getRoles().size());
		assertTrue(entity.get().getRoles().contains(role2));
	}

	@Test
	void saveData_WithNullPrimaryLanguage_HandlesCorrectly() {
		// Arrange
		FluctlightData data = new FluctlightData(
				null,
				new DiscordLocale[0],
				null
		);

		// Act
		adapter.saveData(123L, data);

		// Assert
		Optional<FluctlightEntity> entity = fluctlightRepository.findById(123L);
		assertTrue(entity.isPresent());
		assertNull(entity.get().getPrimaryLanguage());
		assertTrue(entity.get().getAdditionalLanguages().isEmpty());
		assertTrue(entity.get().getRoles().isEmpty());
	}

	@Test
	void existsById_WhenEntityDoesNotExist_ReturnsFalse() {
		// Act
		boolean result = adapter.existsById(999L);

		// Assert
		assertFalse(result);
	}

	@Test
	void existsById_WhenEntityExists_ReturnsTrue() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();

		// Act
		boolean result = adapter.existsById(123L);

		// Assert
		assertTrue(result);
	}

	@Test
	void deleteById_DeletesExistingEntity() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();
		assertTrue(fluctlightRepository.existsById(123L));

		// Act
		adapter.deleteById(123L);

		// Assert
		assertFalse(fluctlightRepository.existsById(123L));
	}

	@Test
	void deleteById_WhenEntityDoesNotExist_NoError() {
		// Act & Assert
		assertDoesNotThrow(() -> adapter.deleteById(999L));
	}

	// Language operations

	@Test
	void updatePrimaryLanguage_WhenEntityExists_UpdatesLanguage() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.primaryLanguage(englishLanguage)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		adapter.updatePrimaryLanguage(123L, DiscordLocale.GERMAN);

		// Assert
		verify(eventPublisher).publishEvent(any(LanguageChangeEvent.class));
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertEquals(germanLanguage.getId(), updated.get().getPrimaryLanguage().getId());
	}

	@Test
	void updatePrimaryLanguage_WhenEntityDoesNotExist_ThrowsException() {
		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.updatePrimaryLanguage(999L, DiscordLocale.ENGLISH_US)
		);
	}

	@Test
	void updatePrimaryLanguage_WhenLanguageDoesNotExist_ThrowsException() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.updatePrimaryLanguage(123L, DiscordLocale.JAPANESE)
		);
	}

	@Test
	void updatePrimaryLanguage_WhenEventIsCancelled_SkipsUpdate() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.primaryLanguage(englishLanguage)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Mock event publisher to cancel the event
		doAnswer(invocation -> {
			LanguageChangeEvent event = invocation.getArgument(0);
			event.setCancelled(true);
			return null;
		}).when(eventPublisher).publishEvent(any(LanguageChangeEvent.class));

		// Act
		adapter.updatePrimaryLanguage(123L, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		// Should still be English because event was cancelled
		assertEquals(englishLanguage.getId(), updated.get().getPrimaryLanguage().getId());
	}

	@Test
	void addAdditionalLanguage_WhenEntityExists_AddsLanguage() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		adapter.addAdditionalLanguage(123L, DiscordLocale.GERMAN);

		// Assert
		verify(eventPublisher).publishEvent(any(AdditionalLanguageAddedEvent.class));
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertTrue(updated.get().getAdditionalLanguages().contains(germanLanguage));
	}

	@Test
	void addAdditionalLanguage_WhenEntityDoesNotExist_ThrowsException() {
		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.addAdditionalLanguage(999L, DiscordLocale.GERMAN)
		);
	}

	@Test
	void addAdditionalLanguage_WhenLanguageDoesNotExist_ThrowsException() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.addAdditionalLanguage(123L, DiscordLocale.JAPANESE)
		);
	}

	@Test
	void addAdditionalLanguage_WhenLanguageAlreadyExists_DoesNotAddDuplicate() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entity.getAdditionalLanguages().add(germanLanguage);
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		adapter.addAdditionalLanguage(123L, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		long count = updated.get().getAdditionalLanguages().stream()
				.filter(lang -> lang.getLocale() == DiscordLocale.GERMAN)
				.count();
		assertEquals(1, count); // Should still be only one
	}

	@Test
	void addAdditionalLanguage_WhenEventIsCancelled_SkipsAdd() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		doAnswer(invocation -> {
			AdditionalLanguageAddedEvent event = invocation.getArgument(0);
			event.setCancelled(true);
			return null;
		}).when(eventPublisher).publishEvent(any(AdditionalLanguageAddedEvent.class));

		// Act
		adapter.addAdditionalLanguage(123L, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertFalse(updated.get().getAdditionalLanguages().contains(germanLanguage));
	}

	@Test
	void removeAdditionalLanguage_WhenLanguageExists_RemovesLanguage() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entity.getAdditionalLanguages().add(germanLanguage);
		entity.getAdditionalLanguages().add(frenchLanguage);
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		adapter.removeAdditionalLanguage(123L, DiscordLocale.GERMAN);

		// Assert
		verify(eventPublisher).publishEvent(any(AdditionalLanguageRemovedEvent.class));
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertFalse(updated.get().getAdditionalLanguages().contains(germanLanguage));
		assertTrue(updated.get().getAdditionalLanguages().contains(frenchLanguage));
	}

	@Test
	void removeAdditionalLanguage_WhenEntityDoesNotExist_ThrowsException() {
		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.removeAdditionalLanguage(999L, DiscordLocale.GERMAN)
		);
	}

	@Test
	void removeAdditionalLanguage_WhenLanguageDoesNotExist_NoError() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entity.getAdditionalLanguages().add(frenchLanguage);
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		adapter.removeAdditionalLanguage(123L, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertTrue(updated.get().getAdditionalLanguages().contains(frenchLanguage));
	}

	@Test
	void removeAdditionalLanguage_WhenEventIsCancelled_SkipsRemove() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entity.getAdditionalLanguages().add(germanLanguage);
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		doAnswer(invocation -> {
			AdditionalLanguageRemovedEvent event = invocation.getArgument(0);
			event.setCancelled(true);
			return null;
		}).when(eventPublisher).publishEvent(any(AdditionalLanguageRemovedEvent.class));

		// Act
		adapter.removeAdditionalLanguage(123L, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertTrue(updated.get().getAdditionalLanguages().contains(germanLanguage));
	}

	// Role operations

	@Test
	void addAllowedRole_WhenEntityExists_AddsRole() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		adapter.addAllowedRole(123L, 100L);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertTrue(updated.get().getRoles().contains(role1));
	}

	@Test
	void addAllowedRole_WhenEntityDoesNotExist_ThrowsException() {
		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.addAllowedRole(999L, 100L)
		);
	}

	@Test
	void addAllowedRole_WhenRoleDoesNotExist_ThrowsException() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entityManager.persist(entity);
		entityManager.flush();

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.addAllowedRole(123L, 999L)
		);
	}

	@Test
	void addAllowedRole_WhenRoleAlreadyExists_DoesNotAddDuplicate() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entity.getRoles().add(role1);
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		adapter.addAllowedRole(123L, 100L);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		long count = updated.get().getRoles().stream()
				.filter(role -> role.getId() == 100L)
				.count();
		assertEquals(1, count); // Should still be only one
	}

	@Test
	void removeAllowedRole_WhenRoleExists_RemovesRole() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entity.getRoles().add(role1);
		entity.getRoles().add(role2);
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		adapter.removeAllowedRole(123L, 100L);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertFalse(updated.get().getRoles().contains(role1));
		assertTrue(updated.get().getRoles().contains(role2));
	}

	@Test
	void removeAllowedRole_WhenEntityDoesNotExist_ThrowsException() {
		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.removeAllowedRole(999L, 100L)
		);
	}

	@Test
	void removeAllowedRole_WhenRoleDoesNotExist_NoError() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.roles(new HashSet<>())
				.build();
		entity.getRoles().add(role2);
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// Act
		adapter.removeAllowedRole(123L, 100L);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertTrue(updated.get().getRoles().contains(role2));
	}

	// Edge cases

	@Test
	void saveData_WithEmptyAdditionalLanguages_HandlesCorrectly() {
		// Arrange
		FluctlightData data = new FluctlightData(
				DiscordLocale.ENGLISH_US,
				new DiscordLocale[0],
				null
		);

		// Act
		adapter.saveData(123L, data);

		// Assert
		Optional<FluctlightEntity> entity = fluctlightRepository.findById(123L);
		assertTrue(entity.isPresent());
		assertTrue(entity.get().getAdditionalLanguages().isEmpty());
	}

	@Test
	void saveData_WithNullAllowedRoles_HandlesCorrectly() {
		// Arrange
		FluctlightData data = new FluctlightData(
				DiscordLocale.ENGLISH_US,
				null,
				null
		);

		// Act
		adapter.saveData(123L, data);

		// Assert
		Optional<FluctlightEntity> entity = fluctlightRepository.findById(123L);
		assertTrue(entity.isPresent());
		assertTrue(entity.get().getRoles().isEmpty());
	}

	@Test
	void multipleOperationsInSequence_WorksCorrectly() {
		// Arrange
		long userId = 123L;

		// Act - Create
		FluctlightData initial = new FluctlightData(
				DiscordLocale.ENGLISH_US,
				new DiscordLocale[]{DiscordLocale.GERMAN},
				new long[]{100L}
		);
		adapter.saveData(userId, initial);

		// Act - Update primary language
		adapter.updatePrimaryLanguage(userId, DiscordLocale.FRENCH);

		// Act - Add additional language
		adapter.addAdditionalLanguage(userId, DiscordLocale.GERMAN); // Already exists, should not duplicate

		// Act - Remove additional language
		adapter.removeAdditionalLanguage(userId, DiscordLocale.GERMAN);

		// Act - Add role
		adapter.addAllowedRole(userId, 200L);

		// Act - Remove role
		adapter.removeAllowedRole(userId, 100L);

		// Assert
		Optional<FluctlightData> finalData = adapter.loadData(userId);
		assertTrue(finalData.isPresent());
		assertEquals(DiscordLocale.FRENCH, finalData.get().getPrimaryLanguage());
		assertEquals(0, finalData.get().getAdditionalLanguages().length);
		assertNotNull(finalData.get().getAllowedRoles());
		assertEquals(1, finalData.get().getAllowedRoles().length);
		assertEquals(200L, finalData.get().getAllowedRoles()[0]);
	}
}

