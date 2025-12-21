package me.whereareiam.yui.common.config.template.messages.command;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.messages.command.PluginCommandMessages;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PluginCommandMessagesTemplate implements TemplateProvider<PluginCommandMessages> {
	@Override
	public PluginCommandMessages supply(PluginCommandMessages plugins) {
		plugins.setDescription("Manage bot plugins");
		plugins.setExample("/yui plugins [action] [plugin]");
		plugins.setVariables(Map.of(
				"action", "The action to perform on a specific plugin",
				"plugin", "The plugin identifier or jar base name (for load)"
		));

		PluginCommandMessages.Controls controls = new PluginCommandMessages.Controls();
		controls.setEnable("Enable");
		controls.setDisable("Disable");
		controls.setLoad("Load");
		controls.setUnload("Unload");
		controls.setReload("Reload list");
		controls.setReloadAll("Reload all");
		plugins.setControls(controls);

		PluginCommandMessages.Main main = new PluginCommandMessages.Main();
		main.setTitle("Plugin Information");
		main.setDescription(List.of(
				"Here you can manage the plugins of the bot.",
				"",
				"Use the buttons below to enable, disable, load or unload plugins.",
				"You can also reload the list of plugins to see any changes made.",
				""
		));
		main.setFormat("<index>. <name> [v<version>]\n <authors>");
		PluginCommandMessages.Main.Fields fields = new PluginCommandMessages.Main.Fields();
		fields.setEnabled("Active [<p:count>]");
		fields.setDisabled("Disabled [<p:count>]");
		fields.setLoadable("Loadable [<p:count>]");
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

		PluginCommandMessages.Category unload = new PluginCommandMessages.Category();
		unload.setTitle("Unload Plugins");
		unload.setEmpty("No plugins available to unload");
		unload.setDescription(List.of(
				"Here you can unload plugins that are currently loaded or enabled.",
				"",
				"<list>"
		));
		unload.setFormat("<index>. <name> [v<version>]\n <authors>");
		plugins.setUnload(unload);

		PluginCommandMessages.Category load = new PluginCommandMessages.Category();
		load.setTitle("Load Plugin");
		load.setEmpty("No loadable plugins found in plugins directory");
		load.setDescription(List.of(
				"Here you can load plugins that are currently not loaded.",
				"",
				"<list>"
		));
		load.setFormat("<index>. <name>");
		plugins.setLoad(load);

		PluginCommandMessages.Action action = new PluginCommandMessages.Action();
		action.setErrorTitle("Action failed for <p:actionName>");
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


