package me.whereareiam.yui.role;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Gives and takes the roles that follow facts about members: the languages they speak, that they are verified, what
 * a plugin records. The owner of a fact calls it when the fact changes; roles follow facts one way, so a role
 * removed by hand stays removed until the fact changes again. Every change is logged and posted as the audit entry
 * {@code member.role}.
 *
 * <pre>{@code
 * factRoles.hold(member.getIdLong(), verifiedRole, true, "verified");
 * }</pre>
 */
public interface FactRoles {
	/**
	 * Makes a member of Yui's guild hold a role, or not.
	 *
	 * @param member the member's user id
	 * @param role   the role's id
	 * @param holds  whether the member holds it now
	 * @param fact   what the role stands for, for the log and the audit trail, such as {@code language de}
	 * @return completes once the role is changed; a member or role Yui cannot find, or a change Discord refuses, is
	 * logged and completes too
	 */
	@NotNull CompletableFuture<Void> hold(long member, @NotNull String role, boolean holds, @NotNull String fact);
}
