package me.whereareiam.yui.onboarding.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Onboarding of the members of Yui's guild: a member picks their languages in a conversation with Yui and, at the
 * end, is verified and gets the verified role. Being verified is a fact Onboarding keeps in its own table, so it
 * outlives the role: a member who rejoins gets the role back without onboarding again.
 *
 * <pre>{@code
 * if (!onboarding.isVerified(member.getIdLong())) onboarding.start(member.getIdLong(), null);
 * }</pre>
 */
public interface OnboardingService {
	/**
	 * @param member a user id
	 * @return whether the member finished onboarding and was not reset since
	 */
	boolean isVerified(long member);

	/**
	 * Starts onboarding for a member who is not verified and not onboarding. A verified member gets the verified
	 * role again instead.
	 *
	 * @param member    the member's user id
	 * @param initiator who started it by hand, or null when Yui did
	 * @return what happened
	 */
	@NotNull CompletableFuture<Start> start(long member, @Nullable Long initiator);

	/**
	 * What {@link #start} did.
	 */
	enum Start {
		/**
		 * A conversation opened and the journey runs.
		 */
		STARTED,
		/**
		 * The member is verified already; they hold the role again.
		 */
		VERIFIED,
		/**
		 * The member is in onboarding already.
		 */
		RUNNING,
		/**
		 * The member is a bot or not in Yui's guild, or no conversation could be opened.
		 */
		REFUSED
	}
}
