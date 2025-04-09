package me.whereareiam.yue.adapter.database.entity.profile;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yue.adapter.database.entity.LanguageEntity;

@Data
@Entity
@Table(name = "yue_profiles_languages")
@SuperBuilder
@NoArgsConstructor
public class ProfileLanguageEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "profile_id", nullable = false)
	private ProfileEntity profileEntity;

	@ManyToOne
	@JoinColumn(name = "language_id", nullable = false)
	private LanguageEntity languageEntity;
}
