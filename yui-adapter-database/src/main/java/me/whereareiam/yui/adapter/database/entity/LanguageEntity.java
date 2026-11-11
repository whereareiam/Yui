package me.whereareiam.yui.adapter.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "yui_languages")
@SuperBuilder
@NoArgsConstructor
public class LanguageEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false, unique = true)
	@Enumerated(EnumType.STRING)
	private DiscordLocale locale;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LanguageEntity other)) return false;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
