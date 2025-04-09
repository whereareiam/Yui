package me.whereareiam.yue.adapter.database.entity.profile;

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
public class ProfileEntity {
	@Id
	private long id;

	@ManyToOne
	@JoinColumn()
	private LanguageEntity primaryLanguage;

	@OneToMany(mappedBy = "profile")
	private Set<ProfileLanguageEntity> additionalLanguages;
}
