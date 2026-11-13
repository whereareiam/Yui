package me.whereareiam.yui.onboarding.service;

import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.onboarding.api.OnboardingService.Start;
import me.whereareiam.yui.onboarding.journey.OnboardingJourneys;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultOnboardingServiceTest {
	private static final long MEMBER = 42L;
	private static final long MODERATOR = 7L;

	private VerifiedMembers verified;
	private OnboardingJourneys journeys;
	private OnboardingAudit audit;
	private User user;
	private DefaultOnboardingService service;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		verified = mock(VerifiedMembers.class);
		journeys = mock(OnboardingJourneys.class);
		audit = mock(OnboardingAudit.class);

		Settings settings = new Settings();
		settings.setGuild("1");
		ObjectProvider<Settings> provider = mock(ObjectProvider.class);
		when(provider.getObject()).thenReturn(settings);

		user = mock(User.class);
		Member member = mock(Member.class);
		when(member.getIdLong()).thenReturn(MEMBER);
		when(member.getUser()).thenReturn(user);
		CacheRestAction<Member> retrieval = mock(CacheRestAction.class);
		when(retrieval.submit()).thenReturn(CompletableFuture.completedFuture(member));
		Guild guild = mock(Guild.class);
		when(guild.retrieveMemberById(MEMBER)).thenReturn(retrieval);
		JDA jda = mock(JDA.class);
		when(jda.getGuildById("1")).thenReturn(guild);

		service = new DefaultOnboardingService(verified, journeys, audit, provider, jda);
	}

	@Test
	void aMemberNotVerified_GetsAConversationAndTheStartIsPosted() {
		when(journeys.start(MEMBER, MODERATOR)).thenReturn(CompletableFuture.completedFuture(true));

		assertEquals(Start.STARTED, service.start(MEMBER, MODERATOR).join());
		verify(audit).post(OnboardingAudit.Entry.STARTED, MEMBER, MODERATOR);
	}

	@Test
	void aVerifiedMember_HoldsTheRoleAgainInstead() {
		when(verified.isVerified(MEMBER)).thenReturn(true);

		assertEquals(Start.VERIFIED, service.start(MEMBER, null).join());
		verify(verified).restore(MEMBER);
		verify(journeys, never()).start(anyLong(), any());
	}

	@Test
	void aMemberInOnboarding_IsLeftAlone() {
		when(journeys.isRunning(MEMBER)).thenReturn(true);

		assertEquals(Start.RUNNING, service.start(MEMBER, null).join());
	}

	@Test
	void aBot_IsNeverVerified() {
		when(user.isBot()).thenReturn(true);

		assertEquals(Start.REFUSED, service.start(MEMBER, null).join());
	}

	@Test
	void noConversation_RefusesWithoutAnAuditEntry() {
		when(journeys.start(MEMBER, null)).thenReturn(CompletableFuture.completedFuture(false));

		assertEquals(Start.REFUSED, service.start(MEMBER, null).join());
		verify(audit, never()).post(any(), anyLong(), any());
	}

	@Test
	void aMemberWhoLeftDuringOnboarding_IsPostedAsAbandoned() {
		when(journeys.cancel(MEMBER)).thenReturn(true);

		service.left(MEMBER);

		verify(audit).post(OnboardingAudit.Entry.ABANDONED, MEMBER);
	}
}
