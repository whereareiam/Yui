package me.whereareiam.yui.common.service.initialization;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.model.config.Roles;
import me.whereareiam.yui.Provider;
import me.whereareiam.yui.service.RoleService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class RolesInitializationService {
	private final Provider<Roles> rolesProvider;
	private final RoleService roleService;
	private final JDA jda;

	@Order(Integer.MIN_VALUE)
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		Guild guild = jda.getGuilds().getFirst();
		if (guild == null) {
			log.warn("No guild is connected in the bot.");
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
				log.info("Removed obsolete role: {} (id={})", role.getName(), role.getIdLong());
			} catch (Exception e) {
				log.warn("Failed to remove role: {}", role.getName(), e);
			}
		}
	}

	private void addMissingConfiguredRoles(Map<String, Long> allowedRoles) {
		allowedRoles.forEach((name, id) -> {
			if (!roleService.roleExists(id)) {
				try {
					roleService.addRole(id);
					log.info("Created new role: {} (id={})", name, id);
				} catch (Exception e) {
					log.warn("Failed to create role: {} (id={})", name, id, e);
				}
			}
		});
	}
}