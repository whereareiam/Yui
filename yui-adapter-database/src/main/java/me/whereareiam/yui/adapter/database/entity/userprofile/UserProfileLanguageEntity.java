package me.whereareiam.yui.adapter.database.entity.userprofile;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.whereareiam.yui.adapter.database.entity.LanguageEntity;

@Data
@Entity
@Table(name = "yui_profiles_languages")
@SuperBuilder
@NoArgsConstructor
public class UserProfileLanguageEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "profile_id", nullable = false)
	private UserProfileEntity userProfileEntity;

	@ManyToOne
	@JoinColumn(name = "language_id", nullable = false)
	private LanguageEntity languageEntity;
}
