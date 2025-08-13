package me.whereareiam.yui.common.service.user;

import me.whereareiam.yui.api.model.profile.UserProfile;
import me.whereareiam.yui.api.output.service.RoleService;
import me.whereareiam.yui.api.output.service.UserProfileService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultUserRoleServiceTest {

	@Mock
	private RoleService roleService;
	
	@Mock
	private UserProfileService userProfileService;
	
	@Mock
	private ExecutorService syncPool;
	
	@Mock
	private JDA jda;
	
	@Mock
	private Guild guild;
	
	@Mock
	private Member member;
	
	@Mock
	private Role role1;
	
	@Mock
	private Role role2;
	
	@Mock
	private Task<List<Member>> memberTask;
	
	private DefaultUserRoleService userRoleService;
	
	@BeforeEach
	void setUp() {
		userRoleService = new DefaultUserRoleService(
			roleService, userProfileService, syncPool, jda
		);
	}
	
	@Test
	void testAddRoleToUser_WithExistingProfile() {
		// Arrange
		long userId = 123L;
		long roleId = 1L;
		UserProfile profile = new UserProfile(userId, null, null, new long[]{});
		
		when(roleService.roleExists(roleId)).thenReturn(true);
		when(userProfileService.getProfile(userId)).thenReturn(Optional.of(profile));
		
		// Act
		userRoleService.addRoleToUser(userId, roleId);
		
		// Assert
		verify(roleService).roleExists(roleId);
		verify(userProfileService).getProfile(userId);
		verify(userProfileService).addRole(userId, roleId);
		verify(syncPool).execute(any());
	}
	
	@Test
	void testAddRoleToUser_WithNonExistentProfile() {
		// Arrange
		long userId = 123L;
		long roleId = 1L;
		UserProfile newProfile = new UserProfile(userId, null, null, new long[]{});
		
		when(roleService.roleExists(roleId)).thenReturn(true);
		when(userProfileService.getProfile(userId)).thenReturn(Optional.empty());
		when(userProfileService.createProfile(userId)).thenReturn(Optional.of(newProfile));
		
		// Act
		userRoleService.addRoleToUser(userId, roleId);
		
		// Assert
		verify(roleService).roleExists(roleId);
		verify(userProfileService).getProfile(userId);
		verify(userProfileService).createProfile(userId);
		verify(userProfileService).addRole(userId, roleId);
		verify(syncPool).execute(any());
	}
	
	@Test
	void testAddRoleToUser_WithNonExistentRole() {
		// Arrange
		long userId = 123L;
		long roleId = 999L;
		
		when(roleService.roleExists(roleId)).thenReturn(false);
		
		// Act
		userRoleService.addRoleToUser(userId, roleId);
		
		// Assert
		verify(roleService).roleExists(roleId);
		verify(userProfileService, never()).getProfile(anyLong());
		verify(syncPool, never()).execute(any());
	}
	
	@Test
	void testAddRoleToUser_WithExistingRole() {
		// Arrange
		long userId = 123L;
		long roleId = 1L;
		UserProfile profile = new UserProfile(userId, null, null, new long[]{1L});
		
		when(roleService.roleExists(roleId)).thenReturn(true);
		when(userProfileService.getProfile(userId)).thenReturn(Optional.of(profile));
		
		// Act
		userRoleService.addRoleToUser(userId, roleId);
		
		// Assert
		verify(roleService).roleExists(roleId);
		verify(userProfileService).getProfile(userId);
		verify(syncPool, never()).execute(any());
	}
	
	@Test
	void testRemoveRoleFromUser() {
		// Arrange
		long userId = 123L;
		long roleId = 1L;
		
		// Act
		userRoleService.removeRoleFromUser(userId, roleId);
		
		// Assert
		verify(userProfileService).removeRole(userId, roleId);
		verify(syncPool).execute(any());
	}
	
	@Test
	void testSyncAll() {
		// Arrange
		when(jda.getGuilds()).thenReturn(List.of(guild));
		when(guild.loadMembers()).thenReturn(memberTask);
		when(memberTask.onSuccess(any())).thenReturn(memberTask);
		when(memberTask.onError(any())).thenReturn(memberTask);
		
		// Act
		userRoleService.syncAll();
		
		// Assert
		verify(guild).loadMembers();
		verify(memberTask).onSuccess(any());
		verify(memberTask).onError(any());
	}
}
