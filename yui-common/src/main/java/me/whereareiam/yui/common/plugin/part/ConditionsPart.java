package me.whereareiam.yui.common.plugin.part;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.conditia.ConditionScope;
import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.Conditions;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.api.model.config.audience.Audiences;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A plugin's condition types and the audiences of its {@code plugins/<id>/audiences.yml}, in a Conditia scope of its
 * id, so they are named {@code <id>:<name>} ({@code audience: verification:verified}) and go with it.
 */
@Component
@Order(2)
@RequiredArgsConstructor
class ConditionsPart implements PluginPart {
	private final Conditions conditions;
	private final Configura configura;

	@Override
	public AutoCloseable attach(ApplicationContext plugin, ExtensionContext extension) {
		ConditionScope scope = conditions.scope(extension.descriptor().getId());
		for (ConditionType<?> type : plugin.getBeansOfType(ConditionType.class).values()) scope.register(type);

		audiences(extension).all().forEach(scope::name);
		return scope;
	}

	private Audiences audiences(ExtensionContext extension) {
		Path file = extension.dataFolder().resolve("audiences" + configura.extension());
		if (!Files.exists(file)) return new Audiences();

		return configura.merge(file.toString(), new Audiences());
	}
}
