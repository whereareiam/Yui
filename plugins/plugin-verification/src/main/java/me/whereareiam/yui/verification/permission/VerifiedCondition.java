package me.whereareiam.yui.verification.permission;

import lombok.RequiredArgsConstructor;
import me.whereareiam.conditia.ConditionType;
import me.whereareiam.conditia.result.Outcome;
import me.whereareiam.yui.api.permission.Actor;
import me.whereareiam.yui.verification.api.VerificationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * {@code verified: true}: the actor finished a verification; {@code verified: false}, they did not.
 */
@Component
@RequiredArgsConstructor
public final class VerifiedCondition implements ConditionType.Single<Actor, VerifiedCondition.Settings> {
	private final VerificationService verification;

	public record Settings(boolean verified) {
	}

	@Override
	public @NotNull String name() {
		return "verified";
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
		return Outcome.when(verification.isVerified(subject.getUser().getIdLong()) == settings.verified());
	}
}
