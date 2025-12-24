package me.whereareiam.yui.common.config.template.messages.command;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.messages.command.UpdateCheckCommandMessages;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UpdateCheckCommandMessagesTemplate implements TemplateProvider<UpdateCheckCommandMessages> {
    @Override
    public UpdateCheckCommandMessages supply(UpdateCheckCommandMessages updateCheck) {
        updateCheck.setDescription("Check for updates to Yui core or loaded plugins");
        updateCheck.setExample("/yui update core");

        UpdateCheckCommandMessages.Variables variables = new UpdateCheckCommandMessages.Variables();
        variables.setTarget("Target to check for updates (core, plugin, or all)");
        updateCheck.setVariables(variables);

        UpdateCheckCommandMessages.Check check = new UpdateCheckCommandMessages.Check();

        UpdateCheckCommandMessages.Check.Core core = new UpdateCheckCommandMessages.Check.Core();
        core.setTitle("Checking Updates");
        core.setDescription(List.of(
                "Checking for updates to Yui core framework...",
                "Results will be available shortly."
        ));
        check.setCore(core);

        UpdateCheckCommandMessages.Check.All all = new UpdateCheckCommandMessages.Check.All();
        all.setTitle("Checking all plugin updates");
        all.setDescription(List.of(
                "Checking for updates to all loaded plugins...",
                "Results will be available shortly."
        ));
        check.setAll(all);

        UpdateCheckCommandMessages.Check.Plugin plugin = new UpdateCheckCommandMessages.Check.Plugin();
        plugin.setTitle("Checking plugin updates");
        plugin.setDescription(List.of(
                "Checking for updates to the selected plugin...",
                "Results will be available shortly."
        ));
        check.setPlugin(plugin);

        UpdateCheckCommandMessages.Check.Error error = new UpdateCheckCommandMessages.Check.Error();
        error.setTitle("Update check failed");
        error.setDescription(List.of(
                "An error occurred while checking for updates.",
                "Please try again later or check the logs for more details."
        ));
        check.setError(error);

        updateCheck.setCheck(check);

        UpdateCheckCommandMessages.Selection selection = new UpdateCheckCommandMessages.Selection();
        selection.setTitle("Select plugin");
        selection.setDescription(List.of("Choose a plugin to check for updates:"));
        selection.setPlaceholder("Select a plugin to check for updates");

        UpdateCheckCommandMessages.Selection.NoPlugins noPlugins = new UpdateCheckCommandMessages.Selection.NoPlugins();
        noPlugins.setTitle("No plugins loaded");
        noPlugins.setDescription(List.of("There are no plugins currently loaded."));
        selection.setNoPlugins(noPlugins);

        updateCheck.setSelection(selection);

        return updateCheck;
    }
}
