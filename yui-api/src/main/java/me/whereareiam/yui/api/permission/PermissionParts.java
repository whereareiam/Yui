package me.whereareiam.yui.api.permission;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.conditia.model.Part;
import net.dv8tion.jda.api.interactions.Interaction;

/**
 * The parts Yui's places supply to Conditia: a command or a button has an {@code actor}, the member who uses it,
 * and an {@code interaction}, where and how it was used. A condition type names the part it is about by type, so
 * a plugin's {@code ConditionType.Single<Actor, Settings>} is asked of the actor.
 *
 * <pre>{@code
 * conditions.scope("onboarding").register(new VerifiedCondition(accounts)); // verified: true, about the actor
 * }</pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PermissionParts {
	public static final Part<Actor> ACTOR = Part.of("actor", Actor.class);
	public static final Part<Interaction> INTERACTION = Part.of("interaction", Interaction.class);
}
