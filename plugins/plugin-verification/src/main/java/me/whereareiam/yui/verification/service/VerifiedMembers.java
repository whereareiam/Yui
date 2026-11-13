package me.whereareiam.yui.verification.service;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.role.FactRoles;
import me.whereareiam.yui.verification.api.MemberVerifiedEvent;
import me.whereareiam.yui.verification.entity.VerifiedMemberEntity;
import me.whereareiam.yui.verification.model.VerificationSettings;
import me.whereareiam.yui.verification.repository.VerifiedMemberRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Owns the fact that a member is verified: keeps it in {@code verification_members} and makes the verified role
 * follow it through core's {@link FactRoles}.
 */
@Component
@RequiredArgsConstructor
public class VerifiedMembers {
	/**
	 * The fact, as the role log and the audit trail name it.
	 */
	public static final String FACT = "verified";

	private final VerifiedMemberRepository members;
	private final FactRoles roles;
	private final ObjectProvider<VerificationSettings> settings;
	private final ApplicationEventPublisher events;
	private final Clock clock = Clock.systemUTC();

	public boolean isVerified(long member) {
		return members.existsById(member);
	}

	/**
	 * Records that the member finished a verification, gives the role and announces it.
	 */
	public void verify(long member) {
		members.save(new VerifiedMemberEntity(member, clock.instant()));
		hold(member, true);
		events.publishEvent(new MemberVerifiedEvent(member));
	}

	/**
	 * Gives the role again to a verified member, such as one who rejoined.
	 */
	public void restore(long member) {
		hold(member, true);
	}

	/**
	 * Forgets that the member was verified and takes the role.
	 */
	public void forget(long member) {
		members.deleteById(member);
		hold(member, false);
	}

	private void hold(long member, boolean holds) {
		String role = settings.getObject().getVerifiedRole();
		if (role == null || role.isBlank()) return;

		roles.hold(member, role, holds, FACT);
	}
}
