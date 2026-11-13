package org.example.tables.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "greeting_words")
@NoArgsConstructor
@AllArgsConstructor
public class WordEntity {
	@Id
	private Long id;

	@Column(nullable = false, length = 64)
	private String word;
}
