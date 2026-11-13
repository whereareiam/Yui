package me.whereareiam.yui.common.permission.type.member;

import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import me.whereareiam.yui.permission.Actor;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code role: "123..."}: the actor holds every listed role, by id.
 */
@Component
public final class RoleCondition implements ConditionType.Single<Actor, RoleCondition.Settings> {
	public record Settings(List<String> roles) {
	}

	@Override
	public @NotNull String name() {
		return "role";
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
				.map(member -> member.getRoles().stream().map(Role::getId).toList().containsAll(settings.roles()))
				.orElse(false));
	}
}
