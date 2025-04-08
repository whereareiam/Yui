package me.whereareiam.yue.adapter.database.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "yue_languages", indexes = {
		@Index(name = "idx_language_code", columnList = "code")
})
public class Language {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "code", nullable = false, unique = true, length = 2)
	private String code;
}
