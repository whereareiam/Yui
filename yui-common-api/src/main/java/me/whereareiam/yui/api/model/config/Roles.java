package me.whereareiam.yui.api.model.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class Roles {
	private Map<String, Long> languageRoles;
	private Map<String, Long> allowedRoles;
}
