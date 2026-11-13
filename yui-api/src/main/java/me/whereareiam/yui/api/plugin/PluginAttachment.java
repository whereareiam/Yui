package me.whereareiam.yui.api.plugin;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import org.springframework.context.ApplicationContext;

/**
 * Hands a plugin's context to Yui while it starts, so Yui registers what the plugin holds; the undoing goes to the
 * plugin's {@link ExtensionContext#onClose}, which runs before the context closes.
 */
@RequiredArgsConstructor
public class PluginAttachment {
	private final ApplicationContext plugin;
	private final ExtensionContext extension;
	private final PluginRegistrations registrations;

	@PostConstruct
	void attach() {
		extension.onClose(registrations.attach(plugin, extension));
	}
}
