package me.whereareiam.yui.onboarding;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.yui.api.persistence.Tables;
import me.whereareiam.yui.api.plugin.Plugin;
import me.whereareiam.yui.api.plugin.YuiPlugin;
import me.whereareiam.yui.onboarding.model.OnboardingSettings;
import me.whereareiam.yui.onboarding.service.DefaultOnboardingService;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Onboarding, a plugin of Yui: a new member picks their languages in a conversation with Yui and is verified at the
 * end. Its folder {@code plugins/onboarding/} holds {@code settings.yml}, {@code commands.yml}, {@code audiences.yml}
 * and {@code messages/<locale>/}.
 */
@Plugin
@Tables
@RequiredArgsConstructor
public class Onboarding implements YuiPlugin {
	/**
	 * The plugin's id, as {@code yui-plugin.yml} has it.
	 */
	public static final String ID = "onboarding";

	private final ObjectProvider<OnboardingSettings> settings;
	private final ObjectProvider<DefaultOnboardingService> onboarding;

	@Override
	public void enable(ExtensionContext context) {
		if (settings.getObject().getTriggers().isStartup()) onboarding.getObject().startAll();
	}

	@Override
	public void disable() {
		onboarding.getObject().cancelAll();
	}
}
