package me.whereareiam.yui.common.scanner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.api.event.plugin.PluginEnabledEvent;
import me.whereareiam.yui.api.model.plugin.InternalPlugin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ListenerScanner {
	private final ApplicationContext rootCtx;
	private final JDA jda;

	public void scan() {
		scan(rootCtx);
	}

	public void scan(ApplicationContext context) {
		for (ListenerAdapter listener : context.getBeansOfType(ListenerAdapter.class).values()) {
			jda.addEventListener(listener);
			log.debug("Registered listener: {}", listener.getClass().getSimpleName());
		}
	}

	@EventListener
	public void onPluginEnabledEvent(PluginEnabledEvent event) {
		InternalPlugin plugin = event.getPlugin();
		scan(plugin.getContext());
	}
}