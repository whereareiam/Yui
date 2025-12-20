package me.whereareiam.yui.adapter.database.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.whereareiam.yui.adapter.database.TestDatabaseConfiguration;
import me.whereareiam.yui.adapter.database.entity.FluctlightEntity;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.entity.RoleEntity;
import me.whereareiam.yui.adapter.database.repository.FluctlightRepository;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.fluctlight.FluctlightData;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for FluctlightPersistenceAdapter using Testcontainers.
 * Tests database operations with a real PostgreSQL database.
 */
@Transactional
@Testcontainers
@SpringBootTest(classes = TestDatabaseConfiguration.class)
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

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private FluctlightRepository fluctlightRepository;

	@Autowired
	private FluctlightPersistenceAdapter adapter;

	private LanguageEntity englishLanguage;
	private LanguageEntity germanLanguage;
	private LanguageEntity frenchLanguage;
	private RoleEntity role1;
	private RoleEntity role2;

	@BeforeEach
	void setUp() {

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

	/**
	 * Helper method to create a Fluctlight with a mocked User for testing.
	 */
	private Fluctlight createFluctlight(long userId) {
		User mockUser = mock(User.class);
		when(mockUser.getIdLong()).thenReturn(userId);
		return new Fluctlight(mockUser);
	}

	// Basic CRUD operations

	@Test
	void loadData_WhenEntityDoesNotExist_ReturnsEmpty() {
		// Arrange
		Fluctlight fluctlight = createFluctlight(999L);

		// Act
		Optional<FluctlightData> result = adapter.loadData(fluctlight);

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
		Fluctlight fluctlight = createFluctlight(123L);
		Optional<FluctlightData> result = adapter.loadData(fluctlight);

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
		Fluctlight fluctlight = createFluctlight(123L);
		adapter.saveData(fluctlight, data);

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
		Fluctlight fluctlight = createFluctlight(123L);
		adapter.saveData(fluctlight, newData);

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
		Fluctlight fluctlight = createFluctlight(123L);
		adapter.saveData(fluctlight, data);

		// Assert
		Optional<FluctlightEntity> entity = fluctlightRepository.findById(123L);
		assertTrue(entity.isPresent());
		assertNull(entity.get().getPrimaryLanguage());
		assertTrue(entity.get().getAdditionalLanguages().isEmpty());
		assertTrue(entity.get().getRoles().isEmpty());
	}

	@Test
	void existsById_WhenEntityDoesNotExist_ReturnsFalse() {
		// Arrange
		Fluctlight fluctlight = createFluctlight(999L);

		// Act
		boolean result = adapter.existsById(fluctlight);

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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		boolean result = adapter.existsById(fluctlight);

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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.deleteById(fluctlight);

		// Assert
		assertFalse(fluctlightRepository.existsById(123L));
	}

	@Test
	void deleteById_WhenEntityDoesNotExist_NoError() {
		// Arrange
		Fluctlight fluctlight = createFluctlight(999L);

		// Act & Assert
		assertDoesNotThrow(() -> adapter.deleteById(fluctlight));
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.updatePrimaryLanguage(fluctlight, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertEquals(germanLanguage.getId(), updated.get().getPrimaryLanguage().getId());
	}

	@Test
	void updatePrimaryLanguage_WhenEntityDoesNotExist_CreatesEntity() {
		// Arrange
		Fluctlight fluctlight = createFluctlight(999L);

		// Act
		adapter.updatePrimaryLanguage(fluctlight, DiscordLocale.ENGLISH_US);

		// Assert
		Optional<FluctlightEntity> entity = fluctlightRepository.findById(999L);
		assertTrue(entity.isPresent());
		assertEquals(englishLanguage.getId(), entity.get().getPrimaryLanguage().getId());
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.updatePrimaryLanguage(fluctlight, DiscordLocale.JAPANESE)
		);
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.addAdditionalLanguage(fluctlight, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertTrue(updated.get().getAdditionalLanguages().contains(germanLanguage));
	}

	@Test
	void addAdditionalLanguage_WhenEntityDoesNotExist_ThrowsException() {
		// Arrange
		Fluctlight fluctlight = createFluctlight(999L);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.addAdditionalLanguage(fluctlight, DiscordLocale.GERMAN)
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.addAdditionalLanguage(fluctlight, DiscordLocale.JAPANESE)
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.addAdditionalLanguage(fluctlight, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		long count = updated.get().getAdditionalLanguages().stream()
				.filter(lang -> lang.getLocale() == DiscordLocale.GERMAN)
				.count();
		assertEquals(1, count); // Should still be only one
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.removeAdditionalLanguage(fluctlight, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertFalse(updated.get().getAdditionalLanguages().contains(germanLanguage));
		assertTrue(updated.get().getAdditionalLanguages().contains(frenchLanguage));
	}

	@Test
	void removeAdditionalLanguage_WhenEntityDoesNotExist_ThrowsException() {
		// Arrange
		Fluctlight fluctlight = createFluctlight(999L);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.removeAdditionalLanguage(fluctlight, DiscordLocale.GERMAN)
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.removeAdditionalLanguage(fluctlight, DiscordLocale.GERMAN);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertTrue(updated.get().getAdditionalLanguages().contains(frenchLanguage));
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.addAllowedRole(fluctlight, 100L);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertTrue(updated.get().getRoles().contains(role1));
	}

	@Test
	void addAllowedRole_WhenEntityDoesNotExist_ThrowsException() {
		// Arrange
		Fluctlight fluctlight = createFluctlight(999L);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.addAllowedRole(fluctlight, 100L)
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.addAllowedRole(fluctlight, 999L)
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.addAllowedRole(fluctlight, 100L);

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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.removeAllowedRole(fluctlight, 100L);

		// Assert
		Optional<FluctlightEntity> updated = fluctlightRepository.findById(123L);
		assertTrue(updated.isPresent());
		assertFalse(updated.get().getRoles().contains(role1));
		assertTrue(updated.get().getRoles().contains(role2));
	}

	@Test
	void removeAllowedRole_WhenEntityDoesNotExist_ThrowsException() {
		// Arrange
		Fluctlight fluctlight = createFluctlight(999L);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () ->
				adapter.removeAllowedRole(fluctlight, 100L)
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

		// Arrange
		Fluctlight fluctlight = createFluctlight(123L);

		// Act
		adapter.removeAllowedRole(fluctlight, 100L);

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
		Fluctlight fluctlight = createFluctlight(123L);
		adapter.saveData(fluctlight, data);

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
		Fluctlight fluctlight = createFluctlight(123L);
		adapter.saveData(fluctlight, data);

		// Assert
		Optional<FluctlightEntity> entity = fluctlightRepository.findById(123L);
		assertTrue(entity.isPresent());
		assertTrue(entity.get().getRoles().isEmpty());
	}

	@Test
	void multipleOperationsInSequence_WorksCorrectly() {
		// Arrange
		long userId = 123L;
		Fluctlight fluctlight = createFluctlight(userId);

		// Act - Create
		FluctlightData initial = new FluctlightData(
				DiscordLocale.ENGLISH_US,
				new DiscordLocale[]{DiscordLocale.GERMAN},
				new long[]{100L}
		);
		adapter.saveData(fluctlight, initial);

		// Act - Update primary language
		adapter.updatePrimaryLanguage(fluctlight, DiscordLocale.FRENCH);

		// Act - Add additional language
		adapter.addAdditionalLanguage(fluctlight, DiscordLocale.GERMAN); // Already exists, should not duplicate

		// Act - Remove additional language
		adapter.removeAdditionalLanguage(fluctlight, DiscordLocale.GERMAN);

		// Act - Add role
		adapter.addAllowedRole(fluctlight, 200L);

		// Act - Remove role
		adapter.removeAllowedRole(fluctlight, 100L);

		// Assert
		Optional<FluctlightData> finalData = adapter.loadData(fluctlight);
		assertTrue(finalData.isPresent());
		assertEquals(DiscordLocale.FRENCH, finalData.get().getPrimaryLanguage());
		assertEquals(0, finalData.get().getAdditionalLanguages().length);
		assertNotNull(finalData.get().getAllowedRoles());
		assertEquals(1, finalData.get().getAllowedRoles().length);
		assertEquals(200L, finalData.get().getAllowedRoles()[0]);
	}
}

