package me.whereareiam.yui;

public final class Constants {
	public static final String PREFIX = "system";
	public static final String VERSION = BuildConfig.VERSION;

	public static final class Structure {
		public static final String pluginsDir = "plugins";
		public static final String stylesDir = "style";
		public static final String messagesDir = "messages";
	}

	/**
	 * Yui's own audit entries; {@code audit.yml} names them to send one elsewhere or nowhere.
	 */
	public static final class AuditEntries {
		public static final String MEMBER_JOIN = "member.join";
		public static final String MEMBER_LEAVE = "member.leave";
		public static final String MEMBER_KICK = "member.kick";
		public static final String MEMBER_ROLE = "member.role";
		public static final String UPDATE_AVAILABLE = "update.available";
		public static final String UPDATE_LOCAL_BUILD = "update.local-build";
	}
}