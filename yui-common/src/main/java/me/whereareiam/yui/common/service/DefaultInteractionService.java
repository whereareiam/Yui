package me.whereareiam.yui.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.model.PayloadButton;
import me.whereareiam.yui.plugin.PluginManager;
import me.whereareiam.yui.service.InteractionService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultInteractionService implements InteractionService, InitializingBean {
	private record Registered<E extends GenericComponentInteractionCreateEvent>(Class<E> type, Consumer<E> consumer) {
	}

	private final JDA jda;
	private final PluginManager pluginManager;
	private final Map<String, Registered<?>> handlers = new ConcurrentHashMap<>();
	private final Map<String, String> payloadStore = new ConcurrentHashMap<>();

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void afterPropertiesSet() {
		jda.addEventListener(new ListenerAdapter() {
			@Override
			public void onGenericComponentInteractionCreate(@NotNull GenericComponentInteractionCreateEvent event) {
				String cid = event.getComponentId();
				String base = cid.contains("|") ? cid.substring(0, cid.indexOf('|')) : cid;

				var r = handlers.get(base);
				if (r != null && r.type().isInstance(event)) {
					((Consumer) r.consumer()).accept(event);
				} else if (log.isDebugEnabled()) {
					log.debug("No interaction handler found for path: {} (event: {})", base, event.getClass().getSimpleName());
				}
			}
		});
		log.info("InteractionService initialized and JDA listener attached");
	}

	private String full(String path) {
		if (path != null && path.indexOf(':') >= 0)
			return path;

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
		String key = full(path);
		Registered<?> previous = handlers.put(key, new Registered<>(type, h));
		if (previous != null) {
			log.warn("Overwriting existing interaction handler for path: {} (previous type: {}, new type: {})",
					key, previous.type().getSimpleName(), type.getSimpleName());
		}

		log.debug("Registered interaction handler for path: {} with type: {}", key, type.getSimpleName());
	}

	@Override
	public void unregister(String pluginId) {
		if (pluginId == null || pluginId.isBlank()) return;

		String prefix = pluginId + ":";

		long handlersBefore = handlers.size();
		handlers.keySet().removeIf(k -> k.startsWith(prefix));
		long handlersAfter = handlers.size();
		long removedHandlers = handlersBefore - handlersAfter;

		long payloadsBefore = payloadStore.size();
		payloadStore.keySet().removeIf(k -> k.startsWith(prefix));
		long payloadsAfter = payloadStore.size();
		long removedPayloads = payloadsBefore - payloadsAfter;

		log.debug("Unregistered {} interaction handler(s) and {} payload(s) for plugin: {}", removedHandlers, removedPayloads, pluginId);
	}

	private Button applyStyle(String path, String label, ButtonStyle style) {
		return Button.of(style, path, label);
	}

	private Button applyStyle(String path, Emoji emoji, ButtonStyle style) {
		return Button.of(style, path, emoji);
	}

	private String pluginId() {
		List<Class<?>> stackClasses = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
				.walk(stream -> {
					List<Class<?>> result = new ArrayList<>();
					stream.skip(2)
							.map(StackWalker.StackFrame::getDeclaringClass)
							.filter(Objects::nonNull)
							.limit(20)
							.forEach(result::add);

					return result;
				});

		for (Class<?> cls : stackClasses) {
			String id = pluginManager.whichPlugin(cls)
					.map(p -> p.getPlugin().getId())
					.orElse(null);

			if (id != null) return id;
		}

		return Constants.PREFIX;
	}
}