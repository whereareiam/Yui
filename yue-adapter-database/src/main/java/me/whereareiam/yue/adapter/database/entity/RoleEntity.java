package me.whereareiam.yue.adapter.database.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yue.adapter.database.entity.userprofile.UserProfileEntity;

import java.util.Set;

@Data
@Entity
@Table(name = "yue_roles")
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoleEntity {
	@Id
	@EqualsAndHashCode.Include
	private long id;

	@ManyToMany(mappedBy = "roles")
	private Set<UserProfileEntity> userProfiles;
}
