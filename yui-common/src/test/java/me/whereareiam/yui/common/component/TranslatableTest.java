package me.whereareiam.yui.common.component;

import me.whereareiam.yui.translation.TranslationService;
import me.whereareiam.yui.translation.Translatable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslatableTest {
	@Mock
	private TranslationService translationService;

	@BeforeEach
	void setUp() throws Exception {
		// Use reflection to set the static field
		setTranslationServiceField(translationService);
	}

	@AfterEach
	void tearDown() throws Exception {
		// Reset the service using reflection
		setTranslationServiceField(null);
	}

	private void setTranslationServiceField(TranslationService service) throws Exception {
		Field field = Translatable.class.getDeclaredField("translationService");
		field.setAccessible(true);
		field.set(null, service);
	}

	@Test
	void of_withUserIdAndKey_shouldCallTranslationService() {
		// Arrange
		when(translationService.translate("vocabulary.test", 123L))
				.thenReturn("Test Translation");

		// Act
		String result = Translatable.of("vocabulary.test", 123L);

		// Assert
		assertEquals("Test Translation", result);
		verify(translationService).translate("vocabulary.test", 123L);
	}

	@Test
	void of_withKeyOnly_shouldUseDefaultUserId() {
		// Arrange
		when(translationService.translate("vocabulary.test", 0L))
				.thenReturn("Default Translation");

		// Act
		String result = Translatable.of("vocabulary.test");

		// Assert
		assertEquals("Default Translation", result);
		verify(translationService).translate("vocabulary.test", 0L);
	}

	@Test
	void of_whenServiceNull_shouldReturnOriginalKey() throws Exception {
		// Arrange
		setTranslationServiceField(null);

		// Act
		String result = Translatable.of("vocabulary.test", 123L);

		// Assert
		assertEquals("vocabulary.test", result);
	}
}