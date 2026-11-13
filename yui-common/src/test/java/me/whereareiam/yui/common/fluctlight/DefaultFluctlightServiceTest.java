package me.whereareiam.yui.common.fluctlight;

import me.whereareiam.yui.api.event.fluctlight.FluctlightClearedEvent;
import me.whereareiam.yui.api.event.fluctlight.FluctlightCreatedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightAdditionalLanguageAddedEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightLanguageChangeEvent;
import me.whereareiam.yui.api.event.fluctlight.language.FluctlightLanguageChangedEvent;
import me.whereareiam.yui.api.fluctlight.FluctlightService;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.persistence.FluctlightPersistence;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Runs {@link DefaultFluctlightService} in a Spring context with transactions, so the after events reach transactional
 * listeners only once a change commits.
 */
class DefaultFluctlightServiceTest {
	private static final long USER = 123L;

	private AnnotationConfigApplicationContext context;
	private FluctlightService service;
	private StoredFacts persistence;
	private Listeners listeners;
	private JDA jda;
	private User user;

	@BeforeEach
	void setUp() {
		context = new AnnotationConfigApplicationContext(TestConfiguration.class);
		service = context.getBean(FluctlightService.class);
		persistence = context.getBean(StoredFacts.class);
		listeners = context.getBean(Listeners.class);
		jda = context.getBean(JDA.class);

		user = mock(User.class);
		when(user.getIdLong()).thenReturn(USER);
		when(jda.getUserById(USER)).thenReturn(user);
	}

	@AfterEach
	void tearDown() {
		context.close();
	}

	@Test
	void get_ANewUser_IsStoredWithTheDefaultLanguageAndAnnounced() {
		Fluctlight fluctlight = service.get(USER).orElseThrow();

		assertEquals(DiscordLocale.ENGLISH_US, fluctlight.getPrimaryLanguage());
		assertEquals(DiscordLocale.ENGLISH_US, persistence.facts.get(USER).primaryLanguage());
		assertInstanceOf(FluctlightCreatedEvent.class, listeners.committed.getFirst());
	}

	@Test
	void get_Twice_ReadsTheCacheTheSecondTime() {
		Fluctlight first = service.get(USER).orElseThrow();
		Fluctlight second = service.get(USER).orElseThrow();

		assertSame(first, second);
		verify(jda, times(1)).getUserById(USER);
	}

	@Test
	void get_AUserOutsideDiscordsCache_IsLoadedFromDiscord() {
		CacheRestAction<User> retrieval = retrieval();
		when(jda.getUserById(USER)).thenReturn(null);
		when(retrieval.complete()).thenReturn(user);

		assertTrue(service.get(USER).isPresent());
	}

	@Test
	void get_AUserDiscordDoesNotKnow_IsEmpty() {
		CacheRestAction<User> retrieval = retrieval();
		ErrorResponseException unknown = mock(ErrorResponseException.class);
		when(unknown.getErrorResponse()).thenReturn(ErrorResponse.UNKNOWN_USER);
		when(jda.getUserById(USER)).thenReturn(null);
		when(retrieval.complete()).thenThrow(unknown);

		assertTrue(service.get(USER).isEmpty());
	}

	@Test
	void changePrimaryLanguage_SavesAndAnnouncesAfterCommit() {
		Fluctlight fluctlight = service.get(USER).orElseThrow();
		listeners.committed.clear();

		Fluctlight changed = service.changePrimaryLanguage(fluctlight, DiscordLocale.GERMAN);

		assertEquals(DiscordLocale.GERMAN, changed.getPrimaryLanguage());
		assertEquals(DiscordLocale.ENGLISH_US, fluctlight.getPrimaryLanguage());
		assertEquals(DiscordLocale.GERMAN, persistence.facts.get(USER).primaryLanguage());
		FluctlightLanguageChangedEvent event = assertInstanceOf(FluctlightLanguageChangedEvent.class, listeners.committed.getFirst());
		assertEquals(DiscordLocale.ENGLISH_US, event.getPreviousLanguage());
	}

	@Test
	void changePrimaryLanguage_ToAnAdditionalLanguage_DropsItFromTheAdditional() {
		Fluctlight fluctlight = service.addAdditionalLanguage(service.get(USER).orElseThrow(), DiscordLocale.GERMAN);

		Fluctlight changed = service.changePrimaryLanguage(fluctlight, DiscordLocale.GERMAN);

		assertEquals(Set.of(), changed.getAdditionalLanguages());
	}

	@Test
	void change_ThenGet_ReturnsTheChangedUser() {
		service.addAdditionalLanguage(service.get(USER).orElseThrow(), DiscordLocale.FRENCH);

		assertEquals(Set.of(DiscordLocale.FRENCH), service.get(USER).orElseThrow().getAdditionalLanguages());
	}

	@Test
	void change_VetoedBeforeIt_SavesNothingAndAnnouncesNothing() {
		Fluctlight fluctlight = service.get(USER).orElseThrow();
		listeners.committed.clear();
		listeners.veto = true;

		Fluctlight result = service.changePrimaryLanguage(fluctlight, DiscordLocale.GERMAN);

		assertEquals(DiscordLocale.ENGLISH_US, result.getPrimaryLanguage());
		assertEquals(DiscordLocale.ENGLISH_US, persistence.facts.get(USER).primaryLanguage());
		assertTrue(listeners.committed.isEmpty());
	}

	@Test
	void change_ThatChangesNothing_AnnouncesNothing() {
		Fluctlight fluctlight = service.get(USER).orElseThrow();
		listeners.committed.clear();

		service.addAdditionalLanguage(fluctlight, DiscordLocale.ENGLISH_US);

		assertTrue(listeners.committed.isEmpty());
	}

	@Test
	void change_WhoseAfterListenerFails_IsKept() {
		Fluctlight fluctlight = service.get(USER).orElseThrow();
		listeners.failAfter = true;

		Fluctlight changed = service.addAdditionalLanguage(fluctlight, DiscordLocale.GERMAN);

		assertEquals(Set.of(DiscordLocale.GERMAN), changed.getAdditionalLanguages());
		assertEquals(Set.of(DiscordLocale.GERMAN), persistence.facts.get(USER).additionalLanguages());
	}

	@Test
	void clear_ResetsToTheDefaults() {
		Fluctlight fluctlight = service.changePrimaryLanguage(service.get(USER).orElseThrow(), DiscordLocale.GERMAN);
		fluctlight = service.addAdditionalLanguage(fluctlight, DiscordLocale.FRENCH);
		listeners.committed.clear();

		Fluctlight cleared = service.clear(fluctlight);

		assertEquals(DiscordLocale.ENGLISH_US, cleared.getPrimaryLanguage());
		assertEquals(Set.of(), cleared.getAdditionalLanguages());
		FluctlightClearedEvent event = assertInstanceOf(FluctlightClearedEvent.class, listeners.committed.getFirst());
		assertEquals(DiscordLocale.GERMAN, event.getPrevious().getPrimaryLanguage());
	}

	@Test
	void change_UsesTheStoredFactsNotTheGivenSnapshot() {
		Fluctlight stale = service.get(USER).orElseThrow();
		service.addAdditionalLanguage(stale, DiscordLocale.GERMAN);

		Fluctlight changed = service.addAdditionalLanguage(stale, DiscordLocale.FRENCH);

		assertEquals(Set.of(DiscordLocale.GERMAN, DiscordLocale.FRENCH), changed.getAdditionalLanguages());
	}

	@SuppressWarnings("unchecked")
	private CacheRestAction<User> retrieval() {
		CacheRestAction<User> retrieval = mock(CacheRestAction.class);
		when(jda.retrieveUserById(USER)).thenReturn(retrieval);

		return retrieval;
	}

	@Configuration
	@EnableTransactionManagement
	@Import({DefaultFluctlightService.class, FluctlightChanges.class, FluctlightCache.class, StoredFacts.class, Listeners.class})
	static class TestConfiguration {
		@Bean
		JDA jda() {
			return mock(JDA.class);
		}

		@Bean
		Settings settings() {
			Settings settings = new Settings();
			settings.setLocale(DiscordLocale.ENGLISH_US);

			return settings;
		}

		@Bean
		PlatformTransactionManager transactionManager() {
			return new LocalTransactions();
		}
	}

	static class StoredFacts implements FluctlightPersistence {
		final Map<Long, Facts> facts = new HashMap<>();

		@Override
		public Optional<Facts> load(long userId) {
			return Optional.ofNullable(facts.get(userId));
		}

		@Override
		public void save(long userId, Facts facts) {
			this.facts.put(userId, facts);
		}
	}

	static class Listeners {
		final List<Object> committed = new ArrayList<>();
		boolean veto;
		boolean failAfter;

		@EventListener
		void onChange(FluctlightLanguageChangeEvent event) {
			event.setCancelled(veto);
		}

		@TransactionalEventListener
		void onCreated(FluctlightCreatedEvent event) {
			committed.add(event);
		}

		@TransactionalEventListener
		void onLanguageChanged(FluctlightLanguageChangedEvent event) {
			committed.add(event);
		}

		@TransactionalEventListener
		void onAdded(FluctlightAdditionalLanguageAddedEvent event) {
			committed.add(event);
			if (failAfter) throw new IllegalStateException("A listener failed");
		}

		@TransactionalEventListener
		void onCleared(FluctlightClearedEvent event) {
			committed.add(event);
		}
	}

	// Transactions without a resource: enough for Spring to run synchronizations and transactional listeners.
	static class LocalTransactions extends AbstractPlatformTransactionManager {
		@Override
		protected Object doGetTransaction() {
			return new Object();
		}

		@Override
		protected void doBegin(Object transaction, TransactionDefinition definition) {
		}

		@Override
		protected void doCommit(DefaultTransactionStatus status) {
		}

		@Override
		protected void doRollback(DefaultTransactionStatus status) {
		}
	}
}
