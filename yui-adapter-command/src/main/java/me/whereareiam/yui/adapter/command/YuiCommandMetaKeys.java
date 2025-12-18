package me.whereareiam.yui.adapter.command;

import org.incendo.cloud.key.CloudKey;

/**
 * Cloud meta keys used by Yui's command system.
 */
public final class YuiCommandMetaKeys {
	/**
	 * Meta key for storing the command definition ID (from @Definition annotation).
	 */
	public static final CloudKey<String> DEFINITION = CloudKey.of("yui:definition", String.class);
}
