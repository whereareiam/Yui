package me.whereareiam.yui.type;

/**
 * Represents the severity level of an audit log entry.
 * <p>
 * Severity determines which StyleKit embed style will be used:
 * <ul>
 *   <li>{@link #INFO} - Normal audit logs (info style)</li>
 *   <li>{@link #WARNING} - Important events that need attention (warning style)</li>
 *   <li>{@link #ERROR} - Severe events or violations (error style)</li>
 * </ul>
 */
public enum AuditSeverity {
	/**
	 * Normal audit logs for informational purposes.
	 * Uses StyleKit info embed style.
	 */
	INFO,

	/**
	 * Important events that need attention but are not critical.
	 * Uses StyleKit warning embed style.
	 */
	WARNING,

	/**
	 * Severe events, violations, or critical issues.
	 * Uses StyleKit error embed style.
	 */
	ERROR
}
