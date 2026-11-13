package me.whereareiam.yui.common.permission.type.member;

import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.api.permission.Actor;

import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code joined: 7d}: the actor joined Yui's guild at least that long ago.
 */
@Component
public final class JoinedCondition implements ConditionType.Single<Actor, JoinedCondition.Settings> {
	public record Settings(Duration ago) {
	}

	@Override
	public @NotNull String name() {
		return "joined";
	}

	@Override
	public @NotNull Class<Actor> subject() {
		return Actor.class;
	}

	@Override
	public @NotNull Class<Settings> settings() {
		return Settings.class;
	}

	@Override
	public @NotNull Outcome holds(@NotNull Actor subject, @NotNull Settings settings) {
		return Outcome.when(subject.member()
				.map(member -> member.getTimeJoined().toInstant().plusSeconds(settings.ago().getSeconds()).isBefore(Instant.now()))
				.orElse(false));
	}
}
