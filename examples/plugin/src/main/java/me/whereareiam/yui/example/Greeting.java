package me.whereareiam.yui.example;

import me.whereareiam.yui.api.plugin.Plugin;
import me.whereareiam.yui.api.plugin.YuiPlugin;

/**
 * An example plugin: {@code /hello} greets whoever runs it. Yui registers its command, texts and settings by itself;
 * the main class only marks the plugin. Its folder {@code plugins/greeting/} may hold {@code settings.yml},
 * {@code commands.yml} and {@code messages/<locale>/commands.yml}.
 */
@Plugin
public class Greeting implements YuiPlugin {
	/**
	 * The plugin's id, as {@code yui-plugin.yml} has it.
	 */
	public static final String ID = "greeting";
}
