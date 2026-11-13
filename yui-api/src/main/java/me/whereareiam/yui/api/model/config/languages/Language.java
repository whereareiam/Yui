package me.whereareiam.yui.api.model.config.languages;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * One language members may declare they speak: the emoji its button shows and, optionally, the role members who
 * speak it get. Its name is a text of {@code messages/<locale>/languages.yml}.
 */
@Getter
@Setter
public class Language {
	private @Nullable String emoji;
	private @Nullable String role;
}
