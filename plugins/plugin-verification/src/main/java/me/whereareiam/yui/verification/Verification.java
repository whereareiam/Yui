package me.whereareiam.yui.verification;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.yui.api.persistence.Tables;
import me.whereareiam.yui.api.plugin.Plugin;
import me.whereareiam.yui.api.plugin.YuiPlugin;
import me.whereareiam.yui.verification.model.VerificationSettings;
import me.whereareiam.yui.verification.service.DefaultVerificationService;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Verification, a plugin of Yui: a new member picks their languages in a conversation with Yui and is verified at the
 * end. Its folder {@code plugins/verification/} holds {@code settings.yml}, {@code commands.yml}, {@code audiences.yml}
 * and {@code messages/<locale>/}.
 */
@Plugin
@Tables
@RequiredArgsConstructor
public class Verification implements YuiPlugin {
	/**
	 * The plugin's id, as {@code yui-plugin.yml} has it.
	 */
	public static final String ID = "verification";

	private final ObjectProvider<VerificationSettings> settings;
	private final ObjectProvider<DefaultVerificationService> verification;

	@Override
	public void enable(ExtensionContext context) {
		if (settings.getObject().getTriggers().isStartup()) verification.getObject().startAll();
	}

	@Override
	public void disable() {
		verification.getObject().cancelAll();
	}
}
