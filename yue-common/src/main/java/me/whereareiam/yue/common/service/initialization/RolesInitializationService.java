package me.whereareiam.yue.common.service.initialization;

import lombok.AllArgsConstructor;
import me.whereareiam.yue.api.model.config.Roles;
import me.whereareiam.yue.api.output.provider.Provider;
import me.whereareiam.yue.api.output.service.RoleService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class RolesInitializationService {
	private static final Logger logger = LoggerFactory.getLogger(RolesInitializationService.class);

	private final Provider<Roles> rolesProvider;
	private final RoleService roleService;
	private final JDA jda;

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		Guild guild = jda.getGuilds().getFirst();
		if (guild == null) {
			logger.warn("No guild is connected in the bot.");
			return;
		}

		Map<String, Long> allowedRoles = rolesProvider.get().getAllowedRoles();
		removeObsoleteRoles(guild, allowedRoles);
		addMissingConfiguredRoles(allowedRoles);
	}

	private void removeObsoleteRoles(Guild guild, Map<String, Long> allowedRoles) {
		List<Role> removableRoles = guild.getRoles().stream()
				.filter(role -> !allowedRoles.containsValue(role.getIdLong()))
				.filter(role -> !role.isManaged() && !role.isPublicRole())
				.filter(role -> roleService.roleExists(role.getIdLong()))
				.toList();

		for (Role role : removableRoles) {
			try {
				roleService.removeRole(role.getIdLong());
				logger.info("Removed obsolete role: {} (id={})", role.getName(), role.getIdLong());
			} catch (Exception e) {
				logger.warn("Failed to remove role: {}", role.getName(), e);
			}
		}
	}

	private void addMissingConfiguredRoles(Map<String, Long> allowedRoles) {
		allowedRoles.forEach((name, id) -> {
			if (!roleService.roleExists(id)) {
				try {
					roleService.addRole(id);
					logger.info("Created new role: {} (id={})", name, id);
				} catch (Exception e) {
					logger.warn("Failed to create role: {} (id={})", name, id, e);
				}
			}
		});
	}
}