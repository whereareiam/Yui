package me.whereareiam.yui.adapter.config.template.messages.command;

import me.whereareiam.yui.api.model.config.messages.command.PluginCommandMessages;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PluginCommandMessagesTemplate implements DefaultConfig<PluginCommandMessages> {
	@Override
	public PluginCommandMessages getDefault() {
		PluginCommandMessages plugins = new PluginCommandMessages();
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
		main.setFormat("{index}. {name} [v{version}]\n {authors}");
		PluginCommandMessages.Main.Fields fields = new PluginCommandMessages.Main.Fields();
		fields.setEnabled("Active [{0}]");
		fields.setDisabled("Disabled [{0}]");
		fields.setLoadable("Loadable [{0}]");
		main.setFields(fields);
		plugins.setMain(main);

		PluginCommandMessages.Category enable = new PluginCommandMessages.Category();
		enable.setTitle("Enable Plugins");
		enable.setEmpty("No plugins available to enable");
		enable.setDescription(List.of(
				"Here you can enable plugins that are currently disabled.",
				"",
				"{list}"
		));
		enable.setFormat("{index}. {name} [v{version}]\n {authors}");
		plugins.setEnable(enable);

		PluginCommandMessages.Category disable = new PluginCommandMessages.Category();
		disable.setTitle("Disable Plugins");
		disable.setEmpty("No plugins available to disable");
		disable.setDescription(List.of(
				"Here you can disable plugins that are currently enabled.",
				"",
				"{list}"
		));
		disable.setFormat("{index}. {name} [v{version}]\n {authors}");
		plugins.setDisable(disable);

		PluginCommandMessages.Category unload = new PluginCommandMessages.Category();
		unload.setTitle("Unload Plugins");
		unload.setEmpty("No plugins available to unload");
		unload.setDescription(List.of(
				"Here you can unload plugins that are currently loaded or enabled.",
				"",
				"{list}"
		));
		unload.setFormat("{index}. {name} [v{version}]\n {authors}");
		plugins.setUnload(unload);

		PluginCommandMessages.Category load = new PluginCommandMessages.Category();
		load.setTitle("Load Plugin");
		load.setEmpty("No loadable plugins found in plugins directory");
		load.setDescription(List.of(
				"Here you can load plugins that are currently not loaded.",
				"",
				"{list}"
		));
		load.setFormat("{index}. {name}");
		plugins.setLoad(load);

		PluginCommandMessages.Action action = new PluginCommandMessages.Action();
		action.setErrorTitle("Action failed for {0}");
		plugins.setAction(action);

		return plugins;
	}
}


