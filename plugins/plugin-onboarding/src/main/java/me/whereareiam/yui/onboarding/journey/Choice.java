package me.whereareiam.yui.onboarding.journey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.model.Key;
import me.whereareiam.yui.api.model.journey.JourneySignal;

import java.util.Arrays;
import java.util.Optional;

/**
 * What a member chooses on a step's message: a language, or to go on. Each choice is a button path and the type of
 * the signal it sends; the step it reaches decides what the choice means there.
 */
@Getter
@RequiredArgsConstructor
public enum Choice {
	LANGUAGE("language"),
	CONTINUE("continue");

	/**
	 * The language a {@link #LANGUAGE} signal carries, as its locale tag.
	 */
	public static final Key<String> LOCALE = Key.of("onboarding.locale", String.class);

	private final String path;

	public JourneySignal.Builder signal() {
		return JourneySignal.builder(path);
	}

	/**
	 * @return the choice a signal stands for, empty for one of another kind
	 */
	public static Optional<Choice> of(JourneySignal signal) {
		return Arrays.stream(values()).filter(choice -> choice.path.equals(signal.getType())).findFirst();
	}
}
