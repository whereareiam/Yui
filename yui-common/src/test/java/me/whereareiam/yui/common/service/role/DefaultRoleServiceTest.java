package me.whereareiam.yui.common.service.role;

import me.whereareiam.yui.common.config.provider.RolesProvider;
import me.whereareiam.yui.common.service.DefaultRoleService;
import me.whereareiam.yui.fluctlight.FluctlightRegistry;
import me.whereareiam.yui.model.config.roles.RoleEntry;
import me.whereareiam.yui.model.config.roles.Roles;
import me.whereareiam.yui.model.config.roles.SyncSettings;
import me.whereareiam.yui.model.config.settings.DiscordSettings;
import me.whereareiam.yui.model.config.settings.Settings;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRoleServiceTest {
	@Mock
	private RolesProvider rolesProvider;
	
	@Mock
	private FluctlightRegistry fluctlightRegistry;
	
	@Mock(strictness = Mock.Strictness.LENIENT)
	private ObjectProvider<Settings> settingsProvider;
	
	@Mock(strictness = Mock.Strictness.LENIENT)
	private JDA jda;
	
	@Mock(strictness = Mock.Strictness.LENIENT)
	private Settings settings;
	
	@Mock(strictness = Mock.Strictness.LENIENT)
	private DiscordSettings discordSettings;
	
	@Mock(strictness = Mock.Strictness.LENIENT)
	private Guild guild;
	
	@Mock
	private Member member;
	
	@Mock
	private Role discordRole1;

	@Mock
	private User jdaUser;
	
	private DefaultRoleService roleService;

	@BeforeEach
	void setUp() {
		roleService = new DefaultRoleService(rolesProvider, fluctlightRegistry, settingsProvider, jda);
		
		when(settingsProvider.getObject()).thenReturn(settings);
		when(settings.getDiscord()).thenReturn(discordSettings);
		when(discordSettings.getGuildId()).thenReturn("123456789");
		when(jda.getGuildById("123456789")).thenReturn(guild);
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
		List<RoleEntry> allRoles = roleService.getAllRoles();
		
		// Assert
		assertEquals(2, allRoles.size());
		// API role should take precedence (same ID, but API version)
		RoleEntry role100 = allRoles.stream().filter(r -> r.getId() == 100L).findFirst().orElseThrow();
		assertEquals("API Role Override", role100.getName());
		assertFalse(role100.isSync());
		
		RoleEntry role200 = allRoles.stream().filter(r -> r.getId() == 200L).findFirst().orElseThrow();
		assertEquals("Config Role 2", role200.getName());
	}
	
	@Test
	void testSyncUserRoles_AddsRolesUserShouldHaveButDoesntHave() {
		// Arrange
		RoleEntry syncRole = new RoleEntry();
		syncRole.setId(100L);
		syncRole.setSync(true);
		
		Roles config = new Roles();
		config.setRoles(List.of(syncRole));
		SyncSettings syncSettings = new SyncSettings();
		config.setSync(syncSettings);
		
		when(rolesProvider.get()).thenReturn(config);
		
		Fluctlight fluctlight = new Fluctlight(jdaUser);
		fluctlight.setAllowedRoles(new long[]{100L}); // User should have role 100
		
		when(jdaUser.getIdLong()).thenReturn(123L);
		when(guild.getMemberById(123L)).thenReturn(member);
		when(member.getRoles()).thenReturn(new ArrayList<>()); // User has no roles in Discord
		
		when(guild.getRoleById(100L)).thenReturn(discordRole1);
		
		// Act
		roleService.syncUserRoles(fluctlight).join();
		
		// Assert
		verify(guild).addRoleToMember(member, discordRole1);
	}
	
	@Test
	void testSyncUserRoles_RemovesRolesUserShouldntHaveButHas() {
		// Arrange
		RoleEntry syncRole = new RoleEntry();
		syncRole.setId(100L);
		syncRole.setSync(true);
		
		Roles config = new Roles();
		config.setRoles(List.of(syncRole));
		SyncSettings syncSettings = new SyncSettings();
		config.setSync(syncSettings);
		
		when(rolesProvider.get()).thenReturn(config);
		
		Fluctlight fluctlight = new Fluctlight(jdaUser);
		fluctlight.setAllowedRoles(null); // User shouldn't have role 100
		
		when(jdaUser.getIdLong()).thenReturn(123L);
		when(guild.getMemberById(123L)).thenReturn(member);
		when(member.getRoles()).thenReturn(List.of(discordRole1)); // User has role 100 in Discord
		when(discordRole1.getIdLong()).thenReturn(100L);
		
		when(guild.getRoleById(100L)).thenReturn(discordRole1);
		
		// Act
		roleService.syncUserRoles(fluctlight).join();
		
		// Assert
		verify(guild).removeRoleFromMember(member, discordRole1);
	}
}

