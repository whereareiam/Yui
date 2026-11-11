package me.whereareiam.yui.adapter.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * JPA entity for persisting Fluctlight custom data to the database.
 * <p>
 * This entity stores only the facts Yui keeps about a user (their languages).
 * JDA User data is never persisted and is always retrieved from JDA's cache.
 */
@Getter
@Setter
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

	@ManyToMany
	@JoinTable(
			name = "yui_fluctlights_languages",
			joinColumns = @JoinColumn(name = "fluctlight_id"),
			inverseJoinColumns = @JoinColumn(name = "language_id"))
	private Set<LanguageEntity> additionalLanguages;
}
