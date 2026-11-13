package me.whereareiam.yui.adapter.command.permission;

import lombok.RequiredArgsConstructor;
import me.whereareiam.conditia.Conditions;
import me.whereareiam.conditia.DeferredPlace;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.Reloadable;
import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.permission.PermissionParts;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The place of each command's {@code requires:}, bound once and kept until its definition reloads. Every command is
 * bound when Yui is ready, so a condition that names an unknown type, audience or part stops startup with its path
 * ({@code commands.clear.requires[0].audience}).
 */
@Component
@RequiredArgsConstructor
public class CommandPlaces implements Reloadable {
	private final Conditions conditions;
	private final CommandDefinitionRegistry definitions;
	private final Registry<Reloadable> reloadables;
	private final Map<String, Bound> places = new ConcurrentHashMap<>();

	@EventListener(ApplicationReadyEvent.class)
	void bindAll() {
		reloadables.register(this);
		definitions.getAll().forEach(this::place);
	}

	/**
	 * @param id         the command's id, such as {@code clear}
	 * @param definition its definition
	 * @return the place its requirements are checked at
	 */
	public @NotNull DeferredPlace place(@NotNull String id, @NotNull CommandDefinition definition) {
		Bound bound = places.compute(id, (ignored, existing) -> {
			if (existing != null && existing.definition() == definition) return existing;
			if (existing != null) existing.place().close();

			DeferredPlace place = conditions.deferredPlace("commands." + id + ".requires", definition.getRequires(),
					PermissionParts.ACTOR, PermissionParts.INTERACTION);
			return new Bound(definition, place);
		});

		return bound.place();
	}

	@Override
	public void reload() {
		places.values().forEach(bound -> bound.place().close());
		places.clear();
		definitions.getAll().forEach(this::place);
	}

	private record Bound(CommandDefinition definition, DeferredPlace place) {
	}
}
