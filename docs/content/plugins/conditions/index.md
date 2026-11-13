---
title: Conditions and audiences
description: Add a condition server owners can use in requires lists, and audiences built from it.
---

A condition is a `ConditionType` bean. It names itself, says what it is about and reads its settings from the `requires:` line:

```java
@Component
@RequiredArgsConstructor
public final class VerifiedCondition implements ConditionType.Single<Actor, VerifiedCondition.Settings> {
	private final OnboardingService onboarding;

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
		return Outcome.when(onboarding.isVerified(subject.getUser().getIdLong()) == settings.verified());
	}
}
```

`Actor` is the member: their user, their membership of the server and what Yui knows of them. A one-field record reads the line's value directly: `verified: true`.

## Audiences

A plugin's own audiences are an `AudienceDefaults` bean; the server owner can change them in `plugins/<id>/audiences.yml`. Their names are bare and are used with the plugin's id:

```java
@Component
@RequiredArgsConstructor
public class OnboardingAudiences implements AudienceDefaults {
	private final ConfigReader reader;

	@Override
	public @NotNull Audiences defaults() {
		return new Audiences(Map.of("verified", reader.parse("- verified: true", Requirements.class)));
	}
}
```

`audience: onboarding:verified` then works in any `requires:`. The plugin's conditions and audiences are removed when it stops.
