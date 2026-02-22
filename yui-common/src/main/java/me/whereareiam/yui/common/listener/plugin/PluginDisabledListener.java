package me.whereareiam.yui.common.listener.plugin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.command.CommandService;
import me.whereareiam.yui.event.plugin.PluginDisabledEvent;
import me.whereareiam.yui.model.plugin.InternalPlugin;
import me.whereareiam.yui.service.InteractionService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class PluginDisabledListener {
	private final InteractionService interactions;
	private final CommandService commandService;
	private final JDA jda;

	@EventListener
	public void onPluginDisabledEvent(PluginDisabledEvent event) {
		InternalPlugin plugin = event.getPlugin();

		for (ListenerAdapter listener : plugin.getContext().getBeansOfType(ListenerAdapter.class).values()) {
			jda.removeEventListener(listener);
			log.debug("Unregistered listener: {}", listener.getClass().getSimpleName());
		}

		// Unregister commands from plugin context
		commandService.unregisterByContext(plugin.getContext());

		if (plugin.getPlugin().getId() != null)
			interactions.unregister(plugin.getPlugin().getId());

		// Note: We keep translations loaded even when plugin is disabled
		// They will be unloaded only when the plugin is fully unloaded
	}
}
