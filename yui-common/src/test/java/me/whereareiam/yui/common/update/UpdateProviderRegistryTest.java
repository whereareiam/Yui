package me.whereareiam.yui.common.update;

import me.whereareiam.yui.event.update.UpdateProviderRegisteredEvent;
import me.whereareiam.yui.event.update.UpdateProviderUnregisteredEvent;
import me.whereareiam.yui.model.update.UpdateSource;
import me.whereareiam.yui.update.UpdateProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for UpdateProviderRegistry.
 */
class UpdateProviderRegistryTest {

    private UpdateProviderRegistry registry;
    private ApplicationEventPublisher mockEventPublisher;

    @BeforeEach
    void setUp() {
        mockEventPublisher = mock(ApplicationEventPublisher.class);
        registry = new UpdateProviderRegistry(mockEventPublisher);
    }

    @Test
    void testRegister_success() {
        UpdateProvider provider = createMockProvider("test-provider");

        registry.register(provider);

        Optional<UpdateProvider> retrieved = registry.get("test-provider");
        assertTrue(retrieved.isPresent());
        assertEquals("test-provider", retrieved.get().getId());
        verify(mockEventPublisher).publishEvent(any(UpdateProviderRegisteredEvent.class));
    }

    @Test
    void testRegister_duplicateThrowsException() {
        UpdateProvider provider1 = createMockProvider("duplicate");
        UpdateProvider provider2 = createMockProvider("duplicate");

        registry.register(provider1);

        assertThrows(IllegalArgumentException.class, () -> registry.register(provider2));
    }

    @Test
    void testUnregister_success() {
        UpdateProvider provider = createMockProvider("test-provider");
        registry.register(provider);

        boolean result = registry.unregister("test-provider");

        assertTrue(result);
        assertFalse(registry.get("test-provider").isPresent());
        verify(mockEventPublisher).publishEvent(any(UpdateProviderUnregisteredEvent.class));
    }

    @Test
    void testUnregister_nonExistentReturnsFalse() {
        boolean result = registry.unregister("non-existent");

        assertFalse(result);
        verify(mockEventPublisher, never()).publishEvent(any(UpdateProviderUnregisteredEvent.class));
    }

    @Test
    void testGet_existingProvider() {
        UpdateProvider provider = createMockProvider("test-provider");
        registry.register(provider);

        Optional<UpdateProvider> retrieved = registry.get("test-provider");

        assertTrue(retrieved.isPresent());
        assertEquals("test-provider", retrieved.get().getId());
    }

    @Test
    void testGet_nonExistentProvider() {
        Optional<UpdateProvider> retrieved = registry.get("non-existent");

        assertFalse(retrieved.isPresent());
    }

    @Test
    void testBy_withUpdateSource() {
        UpdateProvider provider = createMockProvider("github");
        registry.register(provider);

        UpdateSource source = new UpdateSource("github", "username/repo");
        Optional<UpdateProvider> retrieved = registry.by(source);

        assertTrue(retrieved.isPresent());
        assertEquals("github", retrieved.get().getId());
    }

    @Test
    void testAll_multipleProviders() {
        UpdateProvider provider1 = createMockProvider("provider-1");
        UpdateProvider provider2 = createMockProvider("provider-2");
        UpdateProvider provider3 = createMockProvider("provider-3");

        registry.register(provider1);
        registry.register(provider2);
        registry.register(provider3);

        assertEquals(3, registry.all().size());
        assertTrue(registry.all().contains(provider1));
        assertTrue(registry.all().contains(provider2));
        assertTrue(registry.all().contains(provider3));
    }

    @Test
    void testHas_existingProvider() {
        UpdateProvider provider = createMockProvider("test-provider");
        registry.register(provider);

        assertTrue(registry.has("test-provider"));
    }

    @Test
    void testHas_nonExistentProvider() {
        assertFalse(registry.has("non-existent"));
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        // Test concurrent registration
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            int index = i;
            threads[i] = new Thread(() -> {
                UpdateProvider provider = createMockProvider("provider-" + index);
                registry.register(provider);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(10, registry.all().size());
    }

    private UpdateProvider createMockProvider(String id) {
        return new UpdateProvider() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public Optional<String> fetchLatest(UpdateSource source) {
                return Optional.of("1.0.0");
            }

            @Override
            public List<String> fetchRecentUpdates(UpdateSource source, int limit) {
                return List.of("abc123", "def456");
            }
        };
    }
}
