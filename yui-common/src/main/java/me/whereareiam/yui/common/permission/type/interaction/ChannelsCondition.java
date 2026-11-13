package me.whereareiam.yui.common.permission.type.interaction;

import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import net.dv8tion.jda.api.interactions.Interaction;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code channels: "123..."}: the interaction happened in one of the listed channels, by id.
 */
@Component
public final class ChannelsCondition implements ConditionType.Single<Interaction, ChannelsCondition.Settings> {
	public record Settings(List<String> channels) {
	}

	@Override
	public @NotNull String name() {
		return "channels";
	}

	@Override
	public @NotNull Class<Interaction> subject() {
		return Interaction.class;
	}

	@Override
	public @NotNull Class<Settings> settings() {
		return Settings.class;
	}

	@Override
	public @NotNull Outcome holds(@NotNull Interaction subject, @NotNull Settings settings) {
		return Outcome.when(subject.getChannel() != null && settings.channels().contains(subject.getChannel().getId()));
	}
}
