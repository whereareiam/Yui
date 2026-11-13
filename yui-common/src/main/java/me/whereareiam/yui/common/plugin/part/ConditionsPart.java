package me.whereareiam.yui.common.plugin.part;

import lombok.RequiredArgsConstructor;
import me.whereareiam.chassis.ExtensionContext;
import me.whereareiam.conditia.ConditionScope;
import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.Conditions;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.api.model.config.audience.Audiences;
import me.whereareiam.yui.api.permission.AudienceDefaults;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A plugin's condition types and its audiences (its {@link AudienceDefaults} under {@code plugins/<id>/audiences.yml}), in a Conditia scope of its
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

		audiences(plugin, extension).all().forEach(scope::name);
		return scope;
	}

	// The plugin's own defaults (beans of its context, not Yui's), under what its file says.
	private Audiences audiences(ApplicationContext plugin, ExtensionContext extension) {
		Map<String, Requirements> defaults = new LinkedHashMap<>();
		for (AudienceDefaults owned : plugin.getBeansOfType(AudienceDefaults.class).values()) defaults.putAll(owned.defaults().all());

		Path file = extension.dataFolder().resolve("audiences" + configura.extension());
		if (!Files.exists(file)) return new Audiences(defaults);

		return configura.merge(file.toString(), new Audiences(defaults));
	}
}
