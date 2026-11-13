package me.whereareiam.yui.common.plugin.part;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.yui.common.journey.JourneyDefinitionRegistry;
import me.whereareiam.yui.common.scanner.JourneyAnnotationScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * A plugin's journeys.
 */
@Component
@Order(5)
@RequiredArgsConstructor
class JourneysPart implements PluginPart {
	private final JourneyAnnotationScanner scanner;
	private final JourneyDefinitionRegistry journeys;

	@Override
	public AutoCloseable attach(ApplicationContext plugin, ExtensionContext extension) {
		scanner.scan(plugin);

		return () -> journeys.unregisterByContext(plugin);
	}
}
