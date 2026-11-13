package me.whereareiam.yui.common.permission.type.member;

import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import me.whereareiam.yui.permission.Actor;
import net.dv8tion.jda.api.Permission;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code permission: MODERATE_MEMBERS}: the actor has every listed Discord permission in Yui's guild.
 */
@Component
public final class PermissionCondition implements ConditionType.Single<Actor, PermissionCondition.Settings> {
	public record Settings(List<Permission> permissions) {
	}

	@Override
	public @NotNull String name() {
		return "permission";
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
		return Outcome.when(subject.member().map(member -> member.hasPermission(settings.permissions())).orElse(false));
	}
}
