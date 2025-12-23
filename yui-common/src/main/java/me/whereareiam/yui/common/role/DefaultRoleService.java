package me.whereareiam.yui.common.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.common.config.provider.RolesProvider;
import me.whereareiam.yui.model.config.roles.RoleEntry;
import me.whereareiam.yui.model.config.roles.Roles;
import me.whereareiam.yui.model.ManagedRole;
import me.whereareiam.yui.type.RoleSource;
import me.whereareiam.yui.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of RoleService.
 * <p>
 * This service manages role configuration and validation. It does NOT handle
 * fluctlight-specific operations or Discord synchronization - those are handled
 * by FluctlightService and RoleSyncScheduler respectively.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultRoleService implements RoleService {
	private final RolesProvider rolesProvider;

	// API-added roles (in-memory only, not persisted to config)
	private final Map<Long, RoleEntry> temporaryRoles = new ConcurrentHashMap<>();

	@Override
	public boolean isRoleAllowed(long roleId) {
		return getRole(roleId).isPresent();
	}

	@Override
	public boolean isSyncEnabled(long roleId) {
		return getRole(roleId)
				.map(ManagedRole::isSync)
				.orElse(false);
	}

	@Override
	public void addAllowedRole(RoleEntry role) {
		if (role == null || role.getId() == 0) {
			throw new IllegalArgumentException("Role entry must not be null and must have a valid ID");
		}
		
		temporaryRoles.put(role.getId(), role);
		log.info("Added role via API: {} (sync: {})", role.getName() != null ? role.getName() : role.getId(), role.isSync());
	}

	@Override
	public boolean removeAllowedRole(long roleId) {
		RoleEntry removed = temporaryRoles.remove(roleId);
		if (removed != null) {
			log.info("Removed API-added role: {}", removed.getName() != null ? removed.getName() : roleId);
			return true;
		}
		return false;
	}

	@Override
	public boolean updateRoleSync(long roleId, boolean sync) {
		// Try API-added roles first
		RoleEntry apiRole = temporaryRoles.get(roleId);
		if (apiRole != null) {
			apiRole.setSync(sync);
			log.info("Updated sync for API-added role {}: {}", apiRole.getName() != null ? apiRole.getName() : roleId, sync);
			return true;
		}
		
		// Try config roles (note: we can't modify config in memory, but we could update API-added override)
		// For now, we'll just log that config roles can't be modified via API
		Roles config = rolesProvider.get();
		if (config != null && config.getRoles() != null) {
			Optional<RoleEntry> configRole = config.getRoles().stream()
					.filter(r -> r.getId() == roleId)
					.findFirst();

			if (configRole.isPresent()) {
				log.warn("Cannot update sync for config role {} via API. Update roles.yml instead.", roleId);
				return false;
			}
		}
		
		return false;
	}

	@Override
	public List<ManagedRole> getAllRoles() {
		Map<Long, ManagedRole> uniqueRoles = new LinkedHashMap<>();

		// Add config roles first
		Roles config = rolesProvider.get();
		if (config != null && config.getRoles() != null) {
			for (RoleEntry entry : config.getRoles()) {
				if (entry != null) {
					uniqueRoles.put(entry.getId(), new ManagedRole(entry, RoleSource.CONFIG));
				}
			}
		}

		// Add API roles (overwrite config roles with same ID - API takes precedence)
		for (RoleEntry entry : temporaryRoles.values()) {
			uniqueRoles.put(entry.getId(), new ManagedRole(entry, RoleSource.API));
		}

		return new ArrayList<>(uniqueRoles.values());
	}

	@Override
	public Optional<ManagedRole> getRole(long roleId) {
		// Check API-added roles first (they take precedence)
		RoleEntry apiRole = temporaryRoles.get(roleId);
		if (apiRole != null) {
			return Optional.of(new ManagedRole(apiRole, RoleSource.API));
		}

		// Check config roles
		Roles config = rolesProvider.get();
		return config.getRoles().stream()
				.filter(r -> r != null && r.getId() == roleId)
				.findFirst()
				.map(entry -> new ManagedRole(entry, RoleSource.CONFIG));
	}
}