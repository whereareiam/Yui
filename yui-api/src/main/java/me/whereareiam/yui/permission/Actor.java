package me.whereareiam.yui.permission;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Who acts at a place where requirements are checked: the user, their membership of Yui's guild (also in a direct
 * message), and what Yui knows about them. Conditions about members take it.
 */
@RequiredArgsConstructor
public final class Actor {
	@Getter
	private final @NotNull User user;
	private final @Nullable Member member;
	@Getter
	private final @NotNull Fluctlight fluctlight;

	/**
	 * @return the user's membership of Yui's guild, empty when they are not in it
	 */
	public @NotNull Optional<Member> member() {
		return Optional.ofNullable(member);
	}
}
