package me.whereareiam.yui.common.plugin.part;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.yui.api.Constants;
import me.whereareiam.yui.api.message.MessageDefaults;
import me.whereareiam.yui.api.message.MessageLayers;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A plugin's texts: its {@link MessageDefaults} and {@code plugins/<id>/messages/<locale>/}, under its id.
 */
@Component
@Order(1)
@RequiredArgsConstructor
class TextsPart implements PluginPart {
	private final MessageLayers texts;

	@Override
	public AutoCloseable attach(ApplicationContext plugin, ExtensionContext extension) {
		String id = extension.descriptor().getId();
		List<MessageDefaults<?>> defaults = new ArrayList<>();
		for (MessageDefaults<?> file : plugin.getBeansOfType(MessageDefaults.class).values()) defaults.add(file);

		texts.add(id, defaults, extension.dataFolder().resolve(Constants.Structure.messagesDir));
		return () -> texts.remove(id);
	}
}
