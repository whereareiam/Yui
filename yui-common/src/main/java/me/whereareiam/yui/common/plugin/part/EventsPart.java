package me.whereareiam.yui.common.plugin.part;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Yui's events in a plugin's context. Spring hands a plugin's events up to Yui's context but never Yui's down, so each
 * event Yui's context sees (its own and every plugin's) is multicast to the plugin's listeners too, except those the
 * plugin published itself, which its listeners already had, and the contexts' own lifecycle events. Spring wraps a
 * plain event anew on its way up, so the plugin's own are known by their payload, marked on the publishing thread.
 */
@Component
@Order(6)
@RequiredArgsConstructor
class EventsPart implements PluginPart {
	private final ConfigurableApplicationContext yui;

	@Override
	public AutoCloseable attach(ApplicationContext plugin, ExtensionContext extension) {
		ApplicationEventMulticaster listeners = plugin.getBean(
				AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
				ApplicationEventMulticaster.class
		);

		// What the plugin publishes passes its own listeners first, on the publishing thread, then goes up to Yui.
		ThreadLocal<Object> published = new ThreadLocal<>();
		ApplicationListener<ApplicationEvent> mark = event -> published.set(payload(event));
		ApplicationListener<ApplicationEvent> forward = event -> {
			if (event instanceof ApplicationContextEvent) return;

			Object payload = payload(event);
			boolean own = payload == published.get();
			published.remove();
			if (!own) listeners.multicastEvent(event);
			published.remove();
		};

		listeners.addApplicationListener(mark);
		yui.addApplicationListener(forward);
		return () -> {
			yui.removeApplicationListener(forward);
			listeners.removeApplicationListener(mark);
		};
	}

	private static Object payload(ApplicationEvent event) {
		return event instanceof PayloadApplicationEvent<?> wrapped ? wrapped.getPayload() : event;
	}
}
