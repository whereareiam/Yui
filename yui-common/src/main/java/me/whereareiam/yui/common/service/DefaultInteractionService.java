package me.whereareiam.yui.common.service;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.input.InteractionService;
import me.whereareiam.yui.api.model.PayloadButton;
import me.whereareiam.yui.api.output.plugin.PluginManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class DefaultInteractionService implements InteractionService, InitializingBean {
	private static final String INTERNAL = "system";

	private record Registered<E extends GenericComponentInteractionCreateEvent>(Class<E> type, Consumer<E> consumer) {}

	private final JDA jda;
	private final PluginManager pluginManager;
	private final Map<String, Registered<?>> handlers = new ConcurrentHashMap<>();
	private final Map<String, String> payloadStore = new ConcurrentHashMap<>();

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void afterPropertiesSet() {
		jda.addEventListener(new ListenerAdapter() {
			@Override
			public void onGenericComponentInteractionCreate(GenericComponentInteractionCreateEvent event) {
				String cid = event.getComponentId();
				String base = cid.contains("|") ? cid.substring(0, cid.indexOf('|')) : cid;

				var r = handlers.get(base);
				if (r != null && r.type().isInstance(event)) {
					if (!event.isAcknowledged())
						event.deferReply().queue();
					((Consumer) r.consumer()).accept(event);
				}
			}
		});
	}

	private String full(String path) {
		return pluginId() + ":" + path;
	}

	@Override
	public Button createButton(ButtonStyle style, String path, String label) {
		return applyStyle(full(path), label, style);
	}

	@Override
	public Button createButton(ButtonStyle style, String path, Emoji emoji) {
		return applyStyle(full(path), emoji, style);
	}

	@Override
	public PayloadButton createButton(ButtonStyle style, String path, String label, String payload) {
		if (style == ButtonStyle.LINK)
			throw new IllegalArgumentException("Link buttons cannot carry a payload");

		String customId = full(path) + '|' + UUID.randomUUID();
		Button btn = applyStyle(customId, label, style);

		payloadStore.put(customId, payload);
		return new PayloadButton(btn, payload);
	}

	@Override
	public PayloadButton createButton(ButtonStyle style, String path, Emoji emoji, String payload) {
		if (style == ButtonStyle.LINK)
			throw new IllegalArgumentException("Link buttons cannot carry a payload");

		String customId = full(path) + '|' + UUID.randomUUID();
		Button btn = applyStyle(customId, emoji, style);

		payloadStore.put(customId, payload);
		return new PayloadButton(btn, payload);
	}

	@Override
	public StringSelectMenu.Builder createStringSelectMenu(String path) {
		return StringSelectMenu.create(full(path));
	}

	@Override
	public EntitySelectMenu.Builder createEntitySelectMenu(String path, EntitySelectMenu.SelectTarget t) {
		return EntitySelectMenu.create(full(path), t);
	}

	@Override
	public String getPayload(GenericComponentInteractionCreateEvent event) {
		return payloadStore.get(event.getComponentId());
	}

	@Override
	public <E extends GenericComponentInteractionCreateEvent> void registerHandler(String path, Class<E> type, Consumer<E> h) {
		handlers.put(full(path), new Registered<>(type, h));
	}

	private Button applyStyle(String path, String label, ButtonStyle style) {
		return Button.of(style, path, label);
	}

	private Button applyStyle(String path, Emoji emoji, ButtonStyle style) {
		return Button.of(style, path, emoji);
	}

	private String pluginId() {
		Class<?> c = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
				.walk(s -> s.skip(2).findFirst().map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
		if (c == null) return INTERNAL;

		return pluginManager.whichPlugin(c)
				.map(plugin -> plugin.getPlugin().getId())
				.orElse(INTERNAL);
	}
}