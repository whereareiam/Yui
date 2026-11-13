package me.whereareiam.yui.common.plugin.part;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.conditia.ConditionScope;
import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.Conditions;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * A plugin's condition types, in a Conditia scope of its id, so they are named {@code <id>:<name>} and go with it.
 */
@Component
@Order(2)
@RequiredArgsConstructor
class ConditionsPart implements PluginPart {
	private final Conditions conditions;

	@Override
	public AutoCloseable attach(ApplicationContext plugin, ExtensionContext extension) {
		ConditionScope scope = conditions.scope(extension.descriptor().getId());
		for (ConditionType<?> type : plugin.getBeansOfType(ConditionType.class).values()) scope.register(type);

		return scope;
	}
}
