package me.whereareiam.yue.adapter.database.entity.profile;

import jakarta.persistence.*;
import lombok.Data;
import me.whereareiam.yue.adapter.database.entity.Language;

import java.util.Set;

@Data
@Entity
@Table(name = "yue_profiles")
public class Profile {
	@Id
	private long id;

	@ManyToOne
	@JoinColumn(nullable = false)
	private Language language;

	@OneToMany(mappedBy = "profile")
	private Set<ProfileLanguage> additionalLanguages;
}
