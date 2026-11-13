package me.whereareiam.yui.common.audit;

import me.whereareiam.yui.api.model.config.audit.AuditRoute;
import me.whereareiam.yui.api.model.config.audit.AuditTrail;
import me.whereareiam.yui.api.persistence.AuditPersistence;
import me.whereareiam.yui.api.type.audit.AuditCategory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditServiceTest {
	private AuditTrail trail;
	private AuditPersistence persistence;
	private DefaultAuditService service;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		trail = new AuditTrail();
		trail.getCategories().put(AuditCategory.MEMBERS, category("100"));
		trail.getCategories().put(AuditCategory.SYSTEM, category("300"));

		ObjectProvider<AuditTrail> provider = mock(ObjectProvider.class);
		when(provider.getObject()).thenReturn(trail);
		persistence = mock(AuditPersistence.class);
		ObjectProvider<AuditPersistence> stores = mock(ObjectProvider.class);
		when(stores.getIfAvailable()).thenReturn(persistence);
		service = new DefaultAuditService(provider, stores, mock(JDA.class), Clock.fixed(Instant.parse("2026-11-13T12:00:00Z"), ZoneOffset.UTC));
	}

	@Test
	void channel_IsTheCategorysOwn() {
		assertEquals(Optional.of("100"), service.channel(AuditCategory.MEMBERS, "member.join"));
	}

	@Test
	void channel_OfACategoryWithoutOne_IsEmpty() {
		assertFalse(service.isPosted(AuditCategory.MODERATION, "member.kick"));
	}

	@Test
	void channel_OfAnEntrySentElsewhere_IsThatCategorys() {
		trail.getEntries().put("member.join", new AuditRoute.To(AuditCategory.SYSTEM));

		assertEquals(Optional.of("300"), service.channel(AuditCategory.MEMBERS, "member.join"));
	}

	@Test
	void channel_OfAnEntrySwitchedOff_IsEmpty() {
		trail.getEntries().put("member.leave", new AuditRoute.Off());

		assertFalse(service.isPosted(AuditCategory.MEMBERS, "member.leave"));
		assertEquals(Optional.of("100"), service.channel(AuditCategory.MEMBERS, "member.join"));
	}

	@Test
	void withTheStoreOn_AnEntryWithoutAChannelIsKept() {
		trail.getStore().setEnabled(true);

		service.audit(AuditCategory.MODERATION, "member.kick", MessageCreateData.fromContent("Kicked"));

		verify(persistence).save(new AuditPersistence.Record(AuditCategory.MODERATION, "member.kick", Instant.parse("2026-11-13T12:00:00Z"), "Kicked"));
		assertTrue(service.isRecorded(AuditCategory.MODERATION, "member.kick"));
	}

	@Test
	void anEntrySwitchedOff_IsNotKeptEither() {
		trail.getStore().setEnabled(true);
		trail.getEntries().put("member.leave", new AuditRoute.Off());

		service.audit(AuditCategory.MEMBERS, "member.leave", MessageCreateData.fromContent("Left"));

		verify(persistence, never()).save(any());
		assertFalse(service.isRecorded(AuditCategory.MEMBERS, "member.leave"));
	}

	private static AuditTrail.Category category(String channel) {
		AuditTrail.Category category = new AuditTrail.Category();
		category.setChannel(channel);

		return category;
	}
}
