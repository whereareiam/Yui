package me.whereareiam.yui.common.config.template.messages.command;

import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.yui.api.model.config.messages.command.PluginCommandMessages;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PluginCommandMessagesTemplate implements DefaultsProvider<PluginCommandMessages> {
	@Override
	public PluginCommandMessages supply(PluginCommandMessages plugins) {
		plugins.setDescription("Manage bot plugins");
		plugins.setExample("/yui plugin enable Cardinal");
		plugins.setVariables(Map.of(
				"action", "The action to perform on a specific plugin",
				"plugin", "The plugin's id"
		));

		PluginCommandMessages.Controls controls = new PluginCommandMessages.Controls();
		controls.setEnable("Enable");
		controls.setDisable("Disable");
		controls.setReload("Reload");
		controls.setRescan("Rescan");
		controls.setReloadAll("Reload all");
		plugins.setControls(controls);

		PluginCommandMessages.Main main = new PluginCommandMessages.Main();
		main.setTitle("Plugin Information");
		main.setDescription(List.of(
				"Here you can manage the plugins of the bot.",
				"",
				"Use the buttons below to enable, disable or reload plugins.",
				"Rescan the plugins folder to install jars added since.",
				""
		));
		main.setFormat("<index>. <name> [v<version>]\n <authors>");
		PluginCommandMessages.Main.Fields fields = new PluginCommandMessages.Main.Fields();
		fields.setEnabled("Active [{count}]");
		fields.setDisabled("Disabled [{count}]");
		fields.setWaiting("Waiting or failed [{count}]");
		main.setFields(fields);
		plugins.setMain(main);

		PluginCommandMessages.Category enable = new PluginCommandMessages.Category();
		enable.setTitle("Enable Plugins");
		enable.setEmpty("No plugins available to enable");
		enable.setDescription(List.of(
				"Here you can enable plugins that are currently disabled.",
				"",
				"<list>"
		));
		enable.setFormat("<index>. <name> [v<version>]\n <authors>");
		plugins.setEnable(enable);

		PluginCommandMessages.Category disable = new PluginCommandMessages.Category();
		disable.setTitle("Disable Plugins");
		disable.setEmpty("No plugins available to disable");
		disable.setDescription(List.of(
				"Here you can disable plugins that are currently enabled.",
				"",
				"<list>"
		));
		disable.setFormat("<index>. <name> [v<version>]\n <authors>");
		plugins.setDisable(disable);

		PluginCommandMessages.Category reloading = new PluginCommandMessages.Category();
		reloading.setTitle("Reload Plugins");
		reloading.setEmpty("No plugin is running");
		reloading.setDescription(List.of(
				"Reloading reads a plugin's jar again and starts it, and the plugins that depend on it, anew.",
				"",
				"<list>"
		));
		reloading.setFormat("<index>. <name> [v<version>]\n <authors>");
		plugins.setReload(reloading);

		PluginCommandMessages.Category rescan = new PluginCommandMessages.Category();
		rescan.setTitle("Rescanned the Plugins Folder");
		rescan.setEmpty("Every plugin runs or is turned off");
		rescan.setDescription(List.of(
				"Jars added to the plugins folder are installed. These plugins wait or failed:",
				"",
				"<list>"
		));
		rescan.setFormat("<index>. <name>: <reason>");
		plugins.setRescan(rescan);

		PluginCommandMessages.Action action = new PluginCommandMessages.Action();
		action.setErrorTitle("Action failed for {actionName}");
		plugins.setAction(action);

		// Reload action under Action
		PluginCommandMessages.Action.Reload reload = new PluginCommandMessages.Action.Reload();

		PluginCommandMessages.Action.Reload.Confirmation reloadConfirmation = new PluginCommandMessages.Action.Reload.Confirmation();
		reloadConfirmation.setTitle("Confirm Plugins Reload");
		reloadConfirmation.setDescription(List.of(
			"You are about to reload all plugins.",
			"",
			"• Unload all currently loaded plugins",
			"• Reload plugins from disk",
			"• Re-enable plugins based on configuration",
			"",
			"This may briefly interrupt plugin features."
		));
		reload.setConfirmation(reloadConfirmation);

		PluginCommandMessages.Action.Reload.Cancelled reloadCancelled = new PluginCommandMessages.Action.Reload.Cancelled();
		reloadCancelled.setTitle("Plugins Reload Cancelled");
		reloadCancelled.setDescription(List.of(
			"No changes were made."
		));
		reload.setCancelled(reloadCancelled);

		PluginCommandMessages.Action.Reload.Error reloadError = new PluginCommandMessages.Action.Reload.Error();
		reloadError.setTitle("Plugins Reload Failed");
		reloadError.setDescription(List.of(
			"An error occurred while reloading plugins."
		));
		reload.setError(reloadError);

		action.setReload(reload);

		return plugins;
	}
}


