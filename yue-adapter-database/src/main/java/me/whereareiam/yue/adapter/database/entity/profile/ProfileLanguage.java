package me.whereareiam.yue.adapter.database.entity.profile;

import jakarta.persistence.*;
import lombok.Data;
import me.whereareiam.yue.adapter.database.entity.Language;

@Data
@Entity
@Table(name = "yue_profiles_languages")
public class ProfileLanguage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "profile_id", nullable = false)
	private Profile profile;

	@ManyToOne
	@JoinColumn(name = "language_id", nullable = false)
	private Language language;
}
