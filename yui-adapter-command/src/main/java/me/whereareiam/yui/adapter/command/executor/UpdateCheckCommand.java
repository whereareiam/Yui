package me.whereareiam.yui.adapter.command.executor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.annotation.ComponentListener;
import me.whereareiam.yui.annotation.command.Argument;
import me.whereareiam.yui.annotation.command.Command;
import me.whereareiam.yui.annotation.command.Definition;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.plugin.PluginManager;
import me.whereareiam.yui.type.UpdateTarget;
import me.whereareiam.yui.update.UpdateService;
import me.whereareiam.yui.util.Components;
import me.whereareiam.yui.util.style.StyleKit;
import me.whereareiam.yui.util.translation.Translatable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Command for manually checking updates for the core framework and plugins.
 */
@Slf4j
@Component
@AllArgsConstructor
public class UpdateCheckCommand {
    private static final String PLUGIN_SELECT_LISTENER = "update_check_plugin_select";

    private final UpdateService updateService;
    private final PluginManager pluginManager;

    @Definition("update-check")
    @Command("update <target>")
    public void onCommand(Interaction interaction, @Argument("target") UpdateTarget target) {
        interaction.deferReply(true).queue();
        Fluctlight fluctlight = interaction.fluctlight();

        switch (target) {
            case CORE -> checkYui(interaction, fluctlight);
            case ALL -> checkAllPlugins(interaction, fluctlight);
            case PLUGIN -> showPluginSelection(interaction, fluctlight);
        }
    }

    private void checkYui(Interaction interaction, Fluctlight fluctlight) {
        log.debug("Checking core updates (triggered by {})", fluctlight.getJdaUser().getAsTag());

        try {
            updateService.checkUpdates();

            EmbedBuilder embed = StyleKit.embeds().success();
            embed.setTitle(Translatable.text("commands.update_check.check.core.title").resolve(fluctlight));
            embed.setDescription(String.join("\n",
                    Translatable.text("commands.update_check.check.core.description").resolve(fluctlight)));

            interaction.getHook().editOriginalEmbeds(embed.build()).queue();
        } catch (Exception e) {
            handleError(interaction, fluctlight, e);
        }
    }

    private void checkAllPlugins(Interaction interaction, Fluctlight fluctlight) {
        log.debug("Checking all plugin updates (triggered by {})", fluctlight.getJdaUser().getAsTag());

        try {
            updateService.checkAllPluginUpdates();

            EmbedBuilder embed = StyleKit.embeds().success();
            embed.setTitle(Translatable.text("commands.update_check.check.all.title").resolve(fluctlight));
            embed.setDescription(String.join("\n",
                    Translatable.text("commands.update_check.check.all.description").resolve(fluctlight)));

            interaction.getHook().editOriginalEmbeds(embed.build()).queue();
        } catch (Exception e) {
            handleError(interaction, fluctlight, e);
        }
    }

    private void showPluginSelection(Interaction interaction, Fluctlight fluctlight) {
        Collection<InternalPlugin> plugins = pluginManager.plugins();

        if (plugins.isEmpty()) {
            EmbedBuilder embed = StyleKit.embeds().warning();
            embed.setTitle(Translatable.text("commands.update_check.selection.no_plugins.title").resolve(fluctlight));
            embed.setDescription(String.join("\n",
                    Translatable.text("commands.update_check.selection.no_plugins.description").resolve(fluctlight)));
            interaction.getHook().editOriginalEmbeds(embed.build()).queue();
            return;
        }

        List<SelectOption> options = plugins.stream()
                .map(p -> SelectOption.of(
                        p.getDescriptor().getName(),
                        p.getDescriptor().getId()
                ).withDescription("v" + p.getDescriptor().getVersion()))
                .toList();

        StringSelectMenu menu = Components.menu(PLUGIN_SELECT_LISTENER)
                .setPlaceholder(Translatable.text("commands.update_check.selection.placeholder").resolve(fluctlight))
                .addOptions(options)
                .build();

        EmbedBuilder embed = StyleKit.embeds().info();
        embed.setTitle(Translatable.text("commands.update_check.selection.title").resolve(fluctlight));
        embed.setDescription(String.join("\n",
                Translatable.text("commands.update_check.selection.description").resolve(fluctlight)));

        interaction.getHook()
                .editOriginalEmbeds(embed.build())
                .setComponents(ActionRow.of(menu))
                .queue();
    }

    @ComponentListener(PLUGIN_SELECT_LISTENER)
    public void onPluginSelect(Fluctlight fluctlight, StringSelectInteractionEvent event) {
        event.deferEdit().queue();

        String pluginId = event.getValues().getFirst();

        try {
            log.debug("Checking updates for plugin '{}' (triggered by {})",
                    pluginId, fluctlight.getJdaUser().getAsTag());
            updateService.checkPluginUpdates(pluginId);

            EmbedBuilder embed = StyleKit.embeds().success();
            embed.setTitle(Translatable.text("commands.update_check.check.plugin.title").resolve(fluctlight));
            embed.setDescription(String.join("\n",
                    Translatable.text("commands.update_check.check.plugin.description").resolve(fluctlight)));

            event.getHook().editOriginalEmbeds(embed.build()).setComponents().queue();
        } catch (Exception e) {
            log.error("Error checking updates for plugin '{}'", pluginId, e);

            EmbedBuilder errorEmbed = StyleKit.embeds().error();
            errorEmbed.setTitle(Translatable.text("commands.update_check.check.error.title").resolve(fluctlight));
            errorEmbed.setDescription(String.join("\n",
                    Translatable.text("commands.update_check.check.error.description").resolve(fluctlight)));

            event.getHook().editOriginalEmbeds(errorEmbed.build()).setComponents().queue();
        }
    }

    private void handleError(Interaction interaction, Fluctlight fluctlight, Exception e) {
        log.error("Error checking updates", e);

        EmbedBuilder errorEmbed = StyleKit.embeds().error();
        errorEmbed.setTitle(Translatable.text("commands.update_check.check.error.title").resolve(fluctlight));
        errorEmbed.setDescription(String.join("\n",
                Translatable.text("commands.update_check.check.error.description").resolve(fluctlight)));

        interaction.getHook().editOriginalEmbeds(errorEmbed.build()).queue();
    }
}
