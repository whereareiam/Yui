package me.whereareiam.yue.adapter.database.entity.userprofile;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yue.adapter.database.entity.LanguageEntity;
import me.whereareiam.yue.adapter.database.entity.RoleEntity;

import java.util.Set;

@Data
@Entity
@Table(name = "yue_profiles")
@SuperBuilder
@NoArgsConstructor
public class UserProfileEntity {
	@Id
	private long id;

	@ManyToOne
	@JoinColumn(name = "language_id")
	private LanguageEntity primaryLanguage;

	@OneToMany
	@JoinTable(
			name = "yue_profiles_languages",
			joinColumns = @JoinColumn(name = "profile_id"),
			inverseJoinColumns = @JoinColumn(name = "language_id"))
	private Set<LanguageEntity> additionalLanguages;

	@ManyToMany
	@JoinTable(
			name = "yue_profiles_roles",
			joinColumns = @JoinColumn(name = "profile_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<RoleEntity> roles;
}
