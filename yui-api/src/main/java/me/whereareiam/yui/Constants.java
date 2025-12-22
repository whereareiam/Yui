package me.whereareiam.yui;

public final class Constants {
	public static final String PREFIX = "system";
	public static final String VERSION = BuildConfig.VERSION;

	public static final class Structure {
		public static final String pluginsDir = "plugins";
		public static final String stylesDir = "styles";
		public static final String languagesDir = "languages";
	}

	public static final class AuditTypes {
		public static final String USER_JOIN = "user_join";
		public static final String USER_LEAVE = "user_leave";
		public static final String USER_KICK = "user_kick";
	}
}