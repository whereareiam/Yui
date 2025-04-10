package me.whereareiam.yue.adapter.database.entity.userprofile;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yue.adapter.database.entity.LanguageEntity;

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
	private LanguageEntity primaryLanguage;

	@OneToMany(mappedBy = "userProfileEntity")
	private Set<UserProfileLanguageEntity> additionalLanguages;
}
