package me.whereareiam.yui.api.model.config.messages.command;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * The texts of {@code /audit}: the newest kept entries of the audit trail.
 */
@Getter
@Setter
public class AuditCommandMessages {
	private String description;
	private String example;
	private Map<String, String> variables;
	private String title;
	private String empty;
	private String disabled;

	/**
	 * One kept entry: {@code {posted}}, {@code {category}}, {@code {entry}} and {@code {content}}.
	 */
	private String line;
}
