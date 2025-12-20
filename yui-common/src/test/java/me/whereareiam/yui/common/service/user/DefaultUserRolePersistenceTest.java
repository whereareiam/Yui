package me.whereareiam.yui.common.service.user;

import me.whereareiam.yui.model.fluctlight.Fluctlight;
import me.whereareiam.yui.persistence.FluctlightPersistence;
import me.whereareiam.yui.persistence.RolePersistence;
import me.whereareiam.yui.fluctlight.FluctlightService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
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

@SuppressWarnings("unused")
@ExtendWith(MockitoExtension.class)
class DefaultUserRolePersistenceTest {

	@Mock
	private RolePersistence rolePersistence;
	
	@Mock
	private FluctlightService fluctlightService;
	
	@Mock
	private FluctlightPersistence fluctlightPersistence;
	
	@Mock
	private ExecutorService scheduledPool;
	
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
	
	@Mock
	private User jdaUser;
	
	private DefaultUserRoleService userRoleService;
	
	@BeforeEach
	void setUp() {
		userRoleService = new DefaultUserRoleService(
				rolePersistence, fluctlightService, fluctlightPersistence, scheduledPool, jda
		);
	}
	
	@Test
	void testAddRoleToUser_WithExistingFluctlight() {
		// Arrange
		long userId = 123L;
		long roleId = 1L;
		Fluctlight fluctlight = new Fluctlight(jdaUser);
		fluctlight.setAllowedRoles(new long[]{});
		
		when(jdaUser.getIdLong()).thenReturn(userId);
		when(rolePersistence.roleExists(roleId)).thenReturn(true);
		when(fluctlightService.get(userId)).thenReturn(Optional.of(fluctlight));
		
		// Act
		userRoleService.addRoleToUser(userId, roleId);
		
		// Assert
		verify(rolePersistence).roleExists(roleId);
		verify(fluctlightService).get(userId);
		verify(fluctlightPersistence).addAllowedRole(userId, roleId);
		verify(scheduledPool).execute(any());
	}
	
	@Test
	void testAddRoleToUser_WithNonExistentFluctlight() {
		// Arrange
		long userId = 123L;
		long roleId = 1L;
		Fluctlight newFluctlight = new Fluctlight(jdaUser);
		newFluctlight.setAllowedRoles(new long[]{});
		
		when(jdaUser.getIdLong()).thenReturn(userId);
		when(rolePersistence.roleExists(roleId)).thenReturn(true);
		when(fluctlightService.get(userId)).thenReturn(Optional.empty());
		when(fluctlightService.getOrCreate(userId)).thenReturn(newFluctlight);
		
		// Act
		userRoleService.addRoleToUser(userId, roleId);
		
		// Assert
		verify(rolePersistence).roleExists(roleId);
		verify(fluctlightService).get(userId);
		verify(fluctlightService).getOrCreate(userId);
		verify(fluctlightPersistence).addAllowedRole(userId, roleId);
		verify(scheduledPool).execute(any());
	}
	
	@Test
	void testAddRoleToUser_WithNonExistentRole() {
		// Arrange
		long userId = 123L;
		long roleId = 999L;
		
		when(rolePersistence.roleExists(roleId)).thenReturn(false);
		
		// Act
		userRoleService.addRoleToUser(userId, roleId);
		
		// Assert
		verify(rolePersistence).roleExists(roleId);
		verify(fluctlightService, never()).get(anyLong());
		verify(scheduledPool, never()).execute(any());
	}
	
	@Test
	void testAddRoleToUser_WithExistingRole() {
		// Arrange
		long userId = 123L;
		long roleId = 1L;
		Fluctlight fluctlight = new Fluctlight(jdaUser);
		fluctlight.setAllowedRoles(new long[]{1L});
		
		when(jdaUser.getIdLong()).thenReturn(userId);
		when(rolePersistence.roleExists(roleId)).thenReturn(true);
		when(fluctlightService.get(userId)).thenReturn(Optional.of(fluctlight));
		
		// Act
		userRoleService.addRoleToUser(userId, roleId);
		
		// Assert
		verify(rolePersistence).roleExists(roleId);
		verify(fluctlightService).get(userId);
		verify(scheduledPool, never()).execute(any());
	}
	
	@Test
	void testRemoveRoleFromUser() {
		// Arrange
		long userId = 123L;
		long roleId = 1L;
		
		// Act
		userRoleService.removeRoleFromUser(userId, roleId);
		
		// Assert
		verify(fluctlightPersistence).removeAllowedRole(userId, roleId);
		verify(scheduledPool).execute(any());
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
