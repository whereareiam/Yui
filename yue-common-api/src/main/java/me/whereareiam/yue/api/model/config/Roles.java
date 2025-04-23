package me.whereareiam.yue.api.model.config;

import java.util.Map;

public class Roles {
	private Map<String, Long> languageRoles;
	private Map<String, Long> allowedRoles;

	public Map<String, Long> getLanguageRoles() {
		return languageRoles;
	}

	public void setLanguageRoles(Map<String, Long> languageRoles) {
		this.languageRoles = languageRoles;
	}

	public Map<String, Long> getAllowedRoles() {
		return allowedRoles;
	}

	public void setAllowedRoles(Map<String, Long> allowedRoles) {
		this.allowedRoles = allowedRoles;
	}
}
