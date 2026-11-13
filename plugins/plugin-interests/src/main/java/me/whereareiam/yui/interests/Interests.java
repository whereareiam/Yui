package me.whereareiam.yui.interests;

import me.whereareiam.yui.api.persistence.Tables;
import me.whereareiam.yui.api.plugin.Plugin;
import me.whereareiam.yui.api.plugin.YuiPlugin;

/**
 * Interests, a plugin of Yui: members pick the topics they are here for, as a step of onboarding and later with
 * {@code /topics}, and hold each topic's role. Its folder {@code plugins/interests/} holds {@code topics.yml},
 * {@code commands.yml} and {@code messages/<locale>/}.
 */
@Plugin
@Tables
public class Interests implements YuiPlugin {
	/**
	 * The plugin's id, as {@code yui-plugin.yml} has it.
	 */
	public static final String ID = "interests";
}
