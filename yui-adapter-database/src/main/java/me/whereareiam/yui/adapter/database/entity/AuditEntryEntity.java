package me.whereareiam.yui.adapter.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yui.api.type.audit.AuditCategory;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "yui_audit_entries")
@SuperBuilder
@NoArgsConstructor
public class AuditEntryEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AuditCategory category;

	@Column(nullable = false, length = 128)
	private String entry;

	@Column(name = "posted_at", nullable = false)
	private Instant postedAt;

	@Column(nullable = false, length = 4000)
	private String content;
}
