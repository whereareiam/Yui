package me.whereareiam.yui.common.audit;

import me.whereareiam.yui.model.config.audit.AuditRoute;
import me.whereareiam.yui.model.config.audit.AuditTrail;
import me.whereareiam.yui.type.audit.AuditCategory;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditServiceTest {
	private AuditTrail trail;
	private DefaultAuditService service;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		trail = new AuditTrail();
		trail.getCategories().put(AuditCategory.MEMBERS, category("100"));
		trail.getCategories().put(AuditCategory.SYSTEM, category("300"));

		ObjectProvider<AuditTrail> provider = mock(ObjectProvider.class);
		when(provider.getObject()).thenReturn(trail);
		service = new DefaultAuditService(provider, mock(JDA.class));
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

	private static AuditTrail.Category category(String channel) {
		AuditTrail.Category category = new AuditTrail.Category();
		category.setChannel(channel);

		return category;
	}
}
