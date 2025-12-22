package me.whereareiam.yui.adapter.plugin.translation.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.adapter.plugin.translation.PluginTranslationLoader;
import me.whereareiam.yui.event.plugin.PluginEnabledEvent;
import me.whereareiam.yui.event.plugin.PluginUnloadedEvent;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens to plugin lifecycle events and manages plugin translations.
 */
@Slf4j
@Component
@AllArgsConstructor
public class PluginTranslationListener {
    private final PluginTranslationLoader pluginTranslationLoader;

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event) {
        InternalPlugin plugin = event.getPlugin();
        if (plugin.getPlugin().getId() == null) return;

        pluginTranslationLoader.loadPlugin(plugin);
    }

    @EventListener
    public void onPluginUnloaded(PluginUnloadedEvent event) {
        InternalPlugin plugin = event.getPlugin();
        if (plugin.getPlugin().getId() == null) return;

        pluginTranslationLoader.unloadPlugin(plugin);
    }
}
