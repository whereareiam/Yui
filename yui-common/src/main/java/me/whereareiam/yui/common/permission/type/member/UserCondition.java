package me.whereareiam.yui.common.permission.type.member;

import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import me.whereareiam.yui.api.permission.Actor;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code user: "123..."}: the actor is one of the listed users, by id.
 */
@Component
public final class UserCondition implements ConditionType.Single<Actor, UserCondition.Settings> {
	public record Settings(List<String> users) {
	}

	@Override
	public @NotNull String name() {
		return "user";
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
		return Outcome.when(settings.users().contains(subject.getUser().getId()));
	}
}
