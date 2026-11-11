package me.whereareiam.yui.common.fluctlight;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FluctlightCacheTest {
	private final FluctlightCache cache = new FluctlightCache();

	@Test
	void put_PastTheCapacity_DropsTheUserReadLongestAgo() {
		for (long id = 0; id <= FluctlightCache.CAPACITY; id++) {
			cache.put(fluctlight(id));
			if (id == 1) cache.find(0);
		}

		assertEquals(FluctlightCache.CAPACITY, cache.size());
		assertTrue(cache.find(0).isPresent());
		assertTrue(cache.find(1).isEmpty());
	}

	@Test
	void invalidateAfterCommit_WithoutTransaction_DropsTheUserNow() {
		cache.put(fluctlight(7));

		cache.invalidateAfterCommit(7);

		assertTrue(cache.find(7).isEmpty());
	}

	private static Fluctlight fluctlight(long id) {
		User user = mock(User.class);
		when(user.getIdLong()).thenReturn(id);

		return Fluctlight.builder().user(user).primaryLanguage(DiscordLocale.ENGLISH_US).build();
	}
}
