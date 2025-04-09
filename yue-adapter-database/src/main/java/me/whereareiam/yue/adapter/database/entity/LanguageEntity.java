package me.whereareiam.yue.adapter.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Locale;

@Data
@Entity
@Table(name = "yue_languages", indexes = {
		@Index(name = "idx_language_locale", columnList = "locale")
})
@SuperBuilder
@NoArgsConstructor
public class LanguageEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false, unique = true)
	private Locale locale;
}
