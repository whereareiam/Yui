package me.whereareiam.yui.common.permission.type.member;

import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import me.whereareiam.yui.api.model.type.Duration;
import me.whereareiam.yui.api.permission.Actor;

import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code account: 30d}: the actor's Discord account is at least that old.
 */
@Component
public final class AccountCondition implements ConditionType.Single<Actor, AccountCondition.Settings> {
	public record Settings(Duration age) {
	}

	@Override
	public @NotNull String name() {
		return "account";
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
		return Outcome.when(subject.getUser().getTimeCreated().toInstant().plusSeconds(settings.age().getSeconds()).isBefore(Instant.now()));
	}
}
