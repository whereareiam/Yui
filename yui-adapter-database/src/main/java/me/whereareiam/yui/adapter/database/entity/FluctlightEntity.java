package me.whereareiam.yui.adapter.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * JPA entity for persisting Fluctlight custom data to the database.
 * <p>
 * This entity stores only the custom application-specific data (languages, roles).
 * JDA User data is never persisted and is always retrieved from JDA's cache.
 */
@Data
@Entity
@Table(name = "yui_fluctlights")
@SuperBuilder
@NoArgsConstructor
public class FluctlightEntity {
	@Id
	private long id;

	@ManyToOne
	@JoinColumn(name = "language_id")
	private LanguageEntity primaryLanguage;

	@OneToMany
	@JoinTable(
			name = "yui_fluctlights_languages",
			joinColumns = @JoinColumn(name = "fluctlight_id"),
			inverseJoinColumns = @JoinColumn(name = "language_id"))
	private Set<LanguageEntity> additionalLanguages;

	@ManyToMany
	@JoinTable(
			name = "yui_fluctlights_roles",
			joinColumns = @JoinColumn(name = "fluctlight_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<RoleEntity> roles;
}
