package me.whereareiam.yui.common.permission.type.interaction;

import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.Interaction;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code channel-type: [text, thread]}: the interaction happened in a channel of one of the listed kinds.
 */
@Component
public final class ChannelTypeCondition implements ConditionType.Single<Interaction, ChannelTypeCondition.Settings> {
	public record Settings(List<Kind> kinds) {
	}

	/**
	 * A kind of channel; every kind of thread is a {@code thread}.
	 */
	public enum Kind {
		TEXT,
		VOICE,
		THREAD,
		DIRECT,
		ANNOUNCEMENT,
		STAGE,
		FORUM,
		OTHER;

		static Kind of(ChannelType type) {
			return switch (type) {
				case TEXT -> TEXT;
				case VOICE -> VOICE;
				case GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD -> THREAD;
				case PRIVATE, GROUP -> DIRECT;
				case NEWS -> ANNOUNCEMENT;
				case STAGE -> STAGE;
				case FORUM, MEDIA -> FORUM;
				default -> OTHER;
			};
		}
	}

	@Override
	public @NotNull String name() {
		return "channel-type";
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
		return Outcome.when(subject.getChannel() != null && settings.kinds().contains(Kind.of(subject.getChannel().getType())));
	}
}
