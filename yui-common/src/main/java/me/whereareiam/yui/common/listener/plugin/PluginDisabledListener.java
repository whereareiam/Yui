package me.whereareiam.yui.common.listener.plugin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.event.plugin.PluginDisabledEvent;
import me.whereareiam.yui.api.input.InteractionService;
import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class PluginDisabledListener {
	private final InteractionService interactions;
	private final JDA jda;

	@EventListener
	public void onPluginDisabledEvent(PluginDisabledEvent event) {
		InternalPlugin plugin = event.getPlugin();

		for (ListenerAdapter listener : plugin.getContext().getBeansOfType(ListenerAdapter.class).values()) {
			jda.removeEventListener(listener);
			log.debug("Unregistered listener: {}", listener.getClass().getSimpleName());
		}

		if (plugin.getPlugin().getId() != null)
			interactions.unregister(plugin.getPlugin().getId());
	}
}