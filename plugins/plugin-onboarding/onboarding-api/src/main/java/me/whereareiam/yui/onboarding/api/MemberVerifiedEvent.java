package me.whereareiam.yui.onboarding.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A member finished onboarding and is verified, published once that is stored.
 *
 * <pre>{@code
 * @EventListener
 * public void onVerified(MemberVerifiedEvent event) {
 *     // grant what follows from being verified
 * }
 * }</pre>
 */
@Getter
@RequiredArgsConstructor
public class MemberVerifiedEvent {
	/**
	 * The member's user id.
	 */
	private final long member;
}
