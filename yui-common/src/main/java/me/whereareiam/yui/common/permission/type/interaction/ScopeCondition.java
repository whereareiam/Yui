package me.whereareiam.yui.common.permission.type.interaction;

import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code scope: guild}: the interaction happened in Yui's guild, or in a direct message ({@code direct}).
 */
@Component
public final class ScopeCondition implements ConditionType.Single<Interaction, ScopeCondition.Settings> {
	public record Settings(Scope scope) {
	}

	/**
	 * Where an interaction happens.
	 */
	public enum Scope {
		GUILD,
		DIRECT
	}

	@Override
	public @NotNull String name() {
		return "scope";
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
		return Outcome.when(settings.scope() == (subject.isFromGuild() ? Scope.GUILD : Scope.DIRECT));
	}
}
