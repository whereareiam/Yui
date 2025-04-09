package me.whereareiam.yue.api.model.profile;

import java.util.Locale;

public class Profile {
	private final long id;
	private Locale primaryLanguage;
	private Locale[] additionalLanguages;

	public Profile(long id, Locale primaryLanguage, Locale[] additionalLanguages) {
		this.id = id;
		this.primaryLanguage = primaryLanguage;
		this.additionalLanguages = additionalLanguages;
	}

	public long getId() {
		return id;
	}

	public Locale getPrimaryLanguage() {
		return primaryLanguage;
	}

	public void setPrimaryLanguage(Locale primaryLanguage) {
		this.primaryLanguage = primaryLanguage;
	}

	public Locale[] getAdditionalLanguages() {
		return additionalLanguages;
	}

	public void setAdditionalLanguages(Locale[] additionalLanguages) {
		this.additionalLanguages = additionalLanguages;
	}
}
