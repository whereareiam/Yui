package me.whereareiam.yui.common.plugin.part;

import me.whereareiam.chassis.ExtensionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Runs {@link EventsPart} on a real context of Yui and two plugin contexts beneath it.
 */
class EventsPartTest {
	private AnnotationConfigApplicationContext yui;
	private AnnotationConfigApplicationContext first;
	private AnnotationConfigApplicationContext second;
	private AutoCloseable firstForwarding;

	@BeforeEach
	void setUp() {
		yui = new AnnotationConfigApplicationContext();
		yui.refresh();
		first = plugin();
		second = plugin();

		EventsPart part = new EventsPart(yui);
		firstForwarding = part.attach(first, mock(ExtensionContext.class));
		part.attach(second, mock(ExtensionContext.class));
	}

	@AfterEach
	void tearDown() {
		second.close();
		first.close();
		yui.close();
	}

	@Test
	void anEventOfYui_ReachesEveryPlugin() {
		yui.publishEvent("joined");

		assertEquals(List.of("joined"), heard(first));
		assertEquals(List.of("joined"), heard(second));
	}

	@Test
	void anEventOfAPlugin_ReachesItOnceAndTheOthers() {
		first.publishEvent("verified");

		assertEquals(List.of("verified"), heard(first));
		assertEquals(List.of("verified"), heard(second));
	}

	@Test
	void aDetachedPlugin_HearsNoMore() throws Exception {
		firstForwarding.close();

		yui.publishEvent("joined");

		assertEquals(List.of(), heard(first));
	}

	private AnnotationConfigApplicationContext plugin() {
		AnnotationConfigApplicationContext plugin = new AnnotationConfigApplicationContext();
		plugin.setParent(yui);
		plugin.register(Listener.class);
		plugin.refresh();

		return plugin;
	}

	private static List<String> heard(AnnotationConfigApplicationContext plugin) {
		return plugin.getBean(Listener.class).heard;
	}

	static class Listener {
		final List<String> heard = new ArrayList<>();

		@EventListener
		void on(String event) {
			heard.add(event);
		}
	}
}
