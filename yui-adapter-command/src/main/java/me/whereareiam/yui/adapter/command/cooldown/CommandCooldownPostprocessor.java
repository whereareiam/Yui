package me.whereareiam.yui.adapter.command.cooldown;

import me.whereareiam.yui.adapter.command.definition.CommandDefinitionRegistry;
import me.whereareiam.yui.adapter.command.manager.YuiCommandMetaKeys;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.exception.command.CooldownActiveException;
import me.whereareiam.yui.model.command.CommandCooldown;
import me.whereareiam.yui.model.command.CommandDefinition;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessingContext;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds a member back from using a command again before its {@code cooldown.duration} has passed; commands of one
 * {@code cooldown.group} share the wait. Runs after the requirements, so only a use that may run starts a cooldown.
 */
@Component
public class CommandCooldownPostprocessor implements CommandPostprocessor<Interaction> {
	private final CommandDefinitionRegistry definitions;
	private final Clock clock;
	private final Map<Key, Instant> ends = new ConcurrentHashMap<>();

	public CommandCooldownPostprocessor(CommandDefinitionRegistry definitions) {
		this(definitions, Clock.systemUTC());
	}

	CommandCooldownPostprocessor(CommandDefinitionRegistry definitions, Clock clock) {
		this.definitions = definitions;
		this.clock = clock;
	}

	@Override
	public void accept(@NotNull CommandPostprocessingContext<Interaction> context) {
		Optional<String> id = context.command().commandMeta().optional(YuiCommandMetaKeys.DEFINITION);
		Optional<CommandCooldown> cooldown = id.flatMap(definitions::get).map(CommandDefinition::getCooldown)
				.filter(CommandCooldown::isEnabled);
		if (cooldown.isEmpty()) return;

		start(context.commandContext().sender().fluctlight().getId(), id.get(), cooldown.get());
	}

	void start(long member, String command, CommandCooldown cooldown) {
		String group = cooldown.getGroup() == null || cooldown.getGroup().isBlank() ? command : cooldown.getGroup();
		Key key = new Key(member, group);
		Instant now = clock.instant();

		Instant end = ends.get(key);
		if (end != null && end.isAfter(now)) throw new CooldownActiveException(end.getEpochSecond() - now.getEpochSecond() + 1);

		ends.values().removeIf(ended -> !ended.isAfter(now));

		ends.put(key, now.plusSeconds(cooldown.getDuration().getSeconds()));
	}

	private record Key(long member, String group) {
	}
}
