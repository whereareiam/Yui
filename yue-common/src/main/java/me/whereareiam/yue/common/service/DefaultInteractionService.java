package me.whereareiam.yue.common.service;

import me.whereareiam.yue.api.input.InteractionService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class DefaultInteractionService implements InteractionService, InitializingBean {
	private static final String INTERNAL = "system";

	private record Registered<E extends GenericComponentInteractionCreateEvent>(Class<E> type, Consumer<E> consumer) {}

	private final JDA jda;
	private final PluginManager pluginManager;
	private final Map<String, Registered<?>> handlers = new ConcurrentHashMap<>();

	@Autowired
	public DefaultInteractionService(JDA jda, PluginManager pluginManager) {
		this.jda = jda;
		this.pluginManager = pluginManager;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void afterPropertiesSet() {
		jda.addEventListener(new ListenerAdapter() {
			@Override
			public void onGenericComponentInteractionCreate(GenericComponentInteractionCreateEvent event) {
				var r = handlers.get(event.getComponentId());
				if (r != null && r.type().isInstance(event))
					((Consumer) r.consumer()).accept(event);
			}
		});
	}

	private String pluginId() {
		Class<?> c = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
				.walk(s -> s.skip(2).findFirst().map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
		if (c == null) return INTERNAL;
		PluginWrapper w = pluginManager.whichPlugin(c);
		return w == null ? INTERNAL : w.getPluginId();
	}

	private String full(String path) {
		return pluginId() + ":" + path;
	}

	@Override
	public Button createButton(String path, String label) {
		return Button.primary(full(path), label);
	}

	@Override
	public StringSelectMenu createStringSelectMenu(String path, List<SelectOption> o) {
		return StringSelectMenu.create(full(path)).addOptions(o).build();
	}

	@Override
	public EntitySelectMenu createEntitySelectMenu(String path, EntitySelectMenu.SelectTarget t, int min, int max) {
		return EntitySelectMenu.create(full(path), t).setRequiredRange(min, max).build();
	}

	@Override
	public <E extends GenericComponentInteractionCreateEvent> void registerHandler(String path, Class<E> type, Consumer<E> h) {
		handlers.put(full(path), new Registered<>(type, h));
	}
}