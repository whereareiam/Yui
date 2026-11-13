package me.whereareiam.yui.verification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A member who finished a verification.
 */
@Getter
@Entity
@Table(name = "verification_members")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifiedMemberEntity {
	@Id
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "verified_at", nullable = false)
	private Instant verifiedAt;
}
