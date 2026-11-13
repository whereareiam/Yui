package me.whereareiam.yui.onboarding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.model.config.settings.Settings;
import me.whereareiam.yui.onboarding.api.OnboardingService;
import me.whereareiam.yui.onboarding.journey.OnboardingJourneys;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Starts onboarding: a verified member gets the role back, a onboarding one is left alone, anyone else gets a
 * conversation and the journey.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultOnboardingService implements OnboardingService {
	private final VerifiedMembers verified;
	private final OnboardingJourneys journeys;
	private final OnboardingAudit audit;
	private final ObjectProvider<Settings> settings;
	private final JDA jda;

	@Override
	public boolean isVerified(long member) {
		return verified.isVerified(member);
	}

	@Override
	public @NotNull CompletableFuture<Start> start(long member, @Nullable Long initiator) {
		Guild guild = guild();
		if (guild == null) return CompletableFuture.completedFuture(Start.REFUSED);

		return guild.retrieveMemberById(member).submit()
				.thenCompose(found -> start(found, initiator))
				.exceptionally(error -> Start.REFUSED);
	}

	/**
	 * Starts onboarding for every member of the guild who is not verified yet.
	 */
	public void startAll() {
		Guild guild = guild();
		if (guild == null) return;

		guild.loadMembers()
				.onSuccess(members -> members.forEach(member -> start(member, null)))
				.onError(error -> log.warn("[Onboarding] Could not load the members: {}", error.getMessage()));
	}

	/**
	 * The member left: their onboarding ends as abandoned.
	 */
	public void left(long member) {
		if (journeys.cancel(member)) audit.post(OnboardingAudit.Entry.ABANDONED, member);
	}

	/**
	 * The member is no longer verified, and verifies again when {@code verify} says so.
	 */
	public void reset(long member, boolean verify) {
		journeys.cancel(member);
		verified.forget(member);
		if (verify) start(member, null);
	}

	public void cancelAll() {
		journeys.cancelAll();
	}

	private CompletableFuture<Start> start(Member member, @Nullable Long initiator) {
		long id = member.getIdLong();
		if (member.getUser().isBot()) return CompletableFuture.completedFuture(Start.REFUSED);
		if (journeys.isRunning(id)) return CompletableFuture.completedFuture(Start.RUNNING);
		if (verified.isVerified(id)) {
			verified.restore(id);
			return CompletableFuture.completedFuture(Start.VERIFIED);
		}

		return journeys.start(id, initiator).thenApply(started -> {
			if (!started) return Start.REFUSED;

			audit.post(OnboardingAudit.Entry.STARTED, id, initiator);
			return Start.STARTED;
		});
	}

	private @Nullable Guild guild() {
		return jda.getGuildById(settings.getObject().getGuild());
	}
}
