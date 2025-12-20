package me.whereareiam.yui.model.config.languages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguageEntry {
	private boolean enabled = true;
	private Long role;
	private String displayName;
}

