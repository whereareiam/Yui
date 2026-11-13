package me.whereareiam.yui.common.permission.type.member;

import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import me.whereareiam.yui.permission.Actor;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code language: de}: the actor speaks one of the listed languages, as their primary or an additional one.
 */
@Component
public final class LanguageCondition implements ConditionType.Single<Actor, LanguageCondition.Settings> {
	public record Settings(List<DiscordLocale> languages) {
	}

	@Override
	public @NotNull String name() {
		return "language";
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
		return Outcome.when(settings.languages().stream().anyMatch(subject.getFluctlight()::speaks));
	}
}
