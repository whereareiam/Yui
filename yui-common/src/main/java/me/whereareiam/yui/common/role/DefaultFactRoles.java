package me.whereareiam.yui.common.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.Constants;
import me.whereareiam.yui.message.Translatable;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.role.FactRoles;
import me.whereareiam.yui.type.audit.AuditCategory;
import me.whereareiam.yui.util.Audit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Changes roles in Yui's guild for facts, logs each change and posts it as {@code member.role} (category members).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultFactRoles implements FactRoles {
	private final JDA jda;
	private final ObjectProvider<Settings> settings;

	@Override
	public @NotNull CompletableFuture<Void> hold(long member, @NotNull String role, boolean holds, @NotNull String fact) {
		Guild guild = jda.getGuildById(settings.getObject().getGuild());
		Role found = guild == null ? null : guild.getRoleById(role);
		if (found == null) {
			log.warn("[Roles] No role {} in Yui's guild to {} for {}", role, holds ? "give" : "take", fact);
			return CompletableFuture.completedFuture(null);
		}

		UserSnowflake user = UserSnowflake.fromId(member);
		var change = holds ? guild.addRoleToMember(user, found) : guild.removeRoleFromMember(user, found);

		return change.reason(fact).submit()
				.thenAccept(done -> logged(member, found, holds, fact))
				.exceptionally(error -> {
					log.warn("[Roles] Could not {} {} {} for {}: {}", holds ? "give" : "take", member, found.getName(), fact, error.getMessage());
					return null;
				});
	}

	private static void logged(long member, Role role, boolean holds, String fact) {
		log.info("[Roles] {} {} {} for {}", holds ? "Gave" : "Took", member, role.getName(), fact);
		Audit.log(AuditCategory.MEMBERS, Constants.AuditEntries.MEMBER_ROLE)
				.withLocalizedText(locale -> Translatable.text(holds ? "audit.user.role.given" : "audit.user.role.taken")
						.with("mention", "<@" + member + ">")
						.with("role", role.getAsMention())
						.with("fact", fact)
						.resolve(locale))
				.send();
	}
}
