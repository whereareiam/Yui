package me.whereareiam.yui.verification.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Verification of the members of Yui's guild: a member picks their languages in a conversation with Yui and, at the
 * end, is verified and gets the verified role. Being verified is a fact Verification keeps in its own table, so it
 * outlives the role: a member who rejoins gets the role back without verifying again.
 *
 * <pre>{@code
 * if (!verification.isVerified(member.getIdLong())) verification.start(member.getIdLong(), null);
 * }</pre>
 */
public interface VerificationService {
	/**
	 * @param member a user id
	 * @return whether the member finished a verification and was not reset since
	 */
	boolean isVerified(long member);

	/**
	 * Starts a verification for a member who is not verified and not verifying. A verified member gets the verified
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
		 * The member is verifying already.
		 */
		RUNNING,
		/**
		 * The member is a bot or not in Yui's guild, or no conversation could be opened.
		 */
		REFUSED
	}
}
