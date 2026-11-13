package me.whereareiam.yui.common.plugin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.yui.common.plugin.part.PluginPart;
import me.whereareiam.yui.plugin.PluginRegistrations;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Registers what a starting plugin holds, part by part (texts, condition types, listeners, commands, journeys), and
 * undoes it the last first when the plugin stops. A part that fails to undo is logged; the others are still undone.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultPluginRegistrations implements PluginRegistrations {
	private final List<PluginPart> parts;

	@Override
	public @NotNull AutoCloseable attach(@NotNull ApplicationContext plugin, @NotNull ExtensionContext extension) {
		String id = extension.descriptor().getId();
		Deque<AutoCloseable> undo = new ArrayDeque<>();
		parts.forEach(part -> undo.push(part.attach(plugin, extension)));

		log.info("[Plugins] Attached {}", id);
		return () -> close(id, undo);
	}

	private static void close(String id, Deque<AutoCloseable> undo) {
		while (!undo.isEmpty()) {
			try {
				undo.pop().close();
			} catch (Exception exception) {
				log.warn("[Plugins] Could not undo a registration of {}", id, exception);
			}
		}

		log.info("[Plugins] Detached {}", id);
	}
}
