package me.whereareiam.yui.onboarding.model.message;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * {@code messages/<locale>/audit.yml}: the audit entries of onboarding, {@code onboarding.started} and the others.
 * {@code {mention}} is the member; in {@code started}, {@code {initiator}} is who started it by hand, or {@code none}.
 */
@Getter
@Setter
public class AuditMessages {
	private Entry started;
	private Entry completed;
	private Entry failed;
	private Entry abandoned;
	private Entry timeout;

	@Getter
	@Setter
	public static class Entry {
		private String title;
		private List<String> description;
	}
}
