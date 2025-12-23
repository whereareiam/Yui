package me.whereareiam.yui.common.role;

import me.whereareiam.yui.common.config.provider.RolesProvider;
import me.whereareiam.yui.model.config.roles.RoleEntry;
import me.whereareiam.yui.model.config.roles.Roles;
import me.whereareiam.yui.model.ManagedRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRoleServiceTest {
	@Mock
	private RolesProvider rolesProvider;
	
	private DefaultRoleService roleService;

	@BeforeEach
	void setUp() {
		roleService = new DefaultRoleService(rolesProvider);
	}
	
	@Test
	void testGetAllRoles_MergesConfigAndApiRoles_ApiTakesPrecedence() {
		// Arrange
		RoleEntry configRole1 = new RoleEntry();
		configRole1.setId(100L);
		configRole1.setSync(true);
		configRole1.setName("Config Role 1");
		
		RoleEntry configRole2 = new RoleEntry();
		configRole2.setId(200L);
		configRole2.setSync(false);
		configRole2.setName("Config Role 2");
		
		Roles config = new Roles();
		config.setRoles(Arrays.asList(configRole1, configRole2));
		
		when(rolesProvider.get()).thenReturn(config);
		
		RoleEntry apiRoleEntry = new RoleEntry();
		apiRoleEntry.setId(100L);
		apiRoleEntry.setSync(false);
		apiRoleEntry.setName("API Role Override");
		apiRoleEntry.setDescription("API description");
		roleService.addAllowedRole(apiRoleEntry);
		
		// Act
		List<ManagedRole> allRoles = roleService.getAllRoles();
		
		// Assert
		assertEquals(2, allRoles.size());
		// API role should take precedence (same ID, but API version)
		ManagedRole role100 = allRoles.stream().filter(r -> r.getId() == 100L).findFirst().orElseThrow();
		assertEquals("API Role Override", role100.getName());
		assertFalse(role100.isSync());
		assertTrue(role100.isTemporary()); // API role should be temporary
		
		ManagedRole role200 = allRoles.stream().filter(r -> r.getId() == 200L).findFirst().orElseThrow();
		assertEquals("Config Role 2", role200.getName());
		assertFalse(role200.isTemporary()); // Config role should not be temporary
	}
	
}

