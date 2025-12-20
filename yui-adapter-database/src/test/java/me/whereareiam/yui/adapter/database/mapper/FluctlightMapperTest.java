package me.whereareiam.yui.adapter.database.mapper;

import me.whereareiam.yui.adapter.database.entity.FluctlightEntity;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;
import me.whereareiam.yui.adapter.database.entity.RoleEntity;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FluctlightMapper.
 * Tests the mapping logic between FluctlightEntity and FluctlightData.
 */
class FluctlightMapperTest {
	private FluctlightMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new FluctlightMapper();
	}

	@Test
	void extractPrimaryLanguage_WhenPrimaryLanguageExists_ReturnsLocale() {
		// Arrange
		LanguageEntity languageEntity = LanguageEntity.builder()
				.id(1)
				.locale(DiscordLocale.ENGLISH_US)
				.build();
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.primaryLanguage(languageEntity)
				.build();

		// Act
		DiscordLocale result = mapper.extractPrimaryLanguage(entity);

		// Assert
		assertNotNull(result);
		assertEquals(DiscordLocale.ENGLISH_US, result);
	}

	@Test
	void extractPrimaryLanguage_WhenPrimaryLanguageIsNull_ReturnsNull() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.primaryLanguage(null)
				.build();

		// Act
		DiscordLocale result = mapper.extractPrimaryLanguage(entity);

		// Assert
		assertNull(result);
	}

	@Test
	void extractAdditionalLanguages_WhenAdditionalLanguagesExist_ReturnsArray() {
		// Arrange
		LanguageEntity lang1 = LanguageEntity.builder()
				.id(1)
				.locale(DiscordLocale.ENGLISH_US)
				.build();
		LanguageEntity lang2 = LanguageEntity.builder()
				.id(2)
				.locale(DiscordLocale.GERMAN)
				.build();
		Set<LanguageEntity> additionalLanguages = new HashSet<>();
		additionalLanguages.add(lang1);
		additionalLanguages.add(lang2);

		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(additionalLanguages)
				.build();

		// Act
		DiscordLocale[] result = mapper.extractAdditionalLanguages(entity);

		// Assert
		assertNotNull(result);
		assertEquals(2, result.length);
		assertTrue(contains(result, DiscordLocale.ENGLISH_US));
		assertTrue(contains(result, DiscordLocale.GERMAN));
	}

	@Test
	void extractAdditionalLanguages_WhenAdditionalLanguagesIsNull_ReturnsEmptyArray() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(null)
				.build();

		// Act
		DiscordLocale[] result = mapper.extractAdditionalLanguages(entity);

		// Assert
		assertNotNull(result);
		assertEquals(0, result.length);
	}

	@Test
	void extractAdditionalLanguages_WhenAdditionalLanguagesIsEmpty_ReturnsEmptyArray() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(new HashSet<>())
				.build();

		// Act
		DiscordLocale[] result = mapper.extractAdditionalLanguages(entity);

		// Assert
		assertNotNull(result);
		assertEquals(0, result.length);
	}

	@Test
	void extractAdditionalLanguages_WithMultipleLanguages_ReturnsAll() {
		// Arrange
		Set<LanguageEntity> additionalLanguages = new HashSet<>();
		additionalLanguages.add(LanguageEntity.builder().id(1).locale(DiscordLocale.ENGLISH_US).build());
		additionalLanguages.add(LanguageEntity.builder().id(2).locale(DiscordLocale.GERMAN).build());
		additionalLanguages.add(LanguageEntity.builder().id(3).locale(DiscordLocale.FRENCH).build());

		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.additionalLanguages(additionalLanguages)
				.build();

		// Act
		DiscordLocale[] result = mapper.extractAdditionalLanguages(entity);

		// Assert
		assertNotNull(result);
		assertEquals(3, result.length);
		assertTrue(contains(result, DiscordLocale.ENGLISH_US));
		assertTrue(contains(result, DiscordLocale.GERMAN));
		assertTrue(contains(result, DiscordLocale.FRENCH));
	}

	@Test
	void extractAllowedRoles_WhenRolesExist_ReturnsArray() {
		// Arrange
		RoleEntity role1 = RoleEntity.builder().id(100L).build();
		RoleEntity role2 = RoleEntity.builder().id(200L).build();
		Set<RoleEntity> roles = new HashSet<>();
		roles.add(role1);
		roles.add(role2);

		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.roles(roles)
				.build();

		// Act
		long[] result = mapper.extractAllowedRoles(entity);

		// Assert
		assertNotNull(result);
		assertEquals(2, result.length);
		assertTrue(contains(result, 100L));
		assertTrue(contains(result, 200L));
	}

	@Test
	void extractAllowedRoles_WhenRolesIsNull_ReturnsNull() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.roles(null)
				.build();

		// Act
		long[] result = mapper.extractAllowedRoles(entity);

		// Assert
		assertNull(result);
	}

	@Test
	void extractAllowedRoles_WhenRolesIsEmpty_ReturnsNull() {
		// Arrange
		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.roles(new HashSet<>())
				.build();

		// Act
		long[] result = mapper.extractAllowedRoles(entity);

		// Assert
		assertNull(result);
	}

	@Test
	void extractAllowedRoles_WithMultipleRoles_ReturnsAll() {
		// Arrange
		Set<RoleEntity> roles = new HashSet<>();
		roles.add(RoleEntity.builder().id(100L).build());
		roles.add(RoleEntity.builder().id(200L).build());
		roles.add(RoleEntity.builder().id(300L).build());

		FluctlightEntity entity = FluctlightEntity.builder()
				.id(123L)
				.roles(roles)
				.build();

		// Act
		long[] result = mapper.extractAllowedRoles(entity);

		// Assert
		assertNotNull(result);
		assertEquals(3, result.length);
		assertTrue(contains(result, 100L));
		assertTrue(contains(result, 200L));
		assertTrue(contains(result, 300L));
	}

	// Helper methods
	private boolean contains(DiscordLocale[] array, DiscordLocale value) {
		for (DiscordLocale locale : array) {
			if (locale == value) {
				return true;
			}
		}
		return false;
	}

	private boolean contains(long[] array, long value) {
		for (long item : array) {
			if (item == value) {
				return true;
			}
		}
		return false;
	}
}

