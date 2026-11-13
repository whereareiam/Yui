package me.whereareiam.yui.adapter.command.permission;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.whereareiam.conditia.adapter.configura.ConditiaFeature;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.model.command.CommandCooldown;
import me.whereareiam.yui.model.command.CommandDefinition;
import me.whereareiam.yui.model.config.audience.Audiences;
import me.whereareiam.yui.model.type.Duration;
import me.whereareiam.yui.type.CommandCategory;
import net.dv8tion.jda.api.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandDefaultPermissionsTest {
	private ObjectMapper mapper;
	private CommandDefaultPermissions permissions;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() throws Exception {
		mapper = Configura.builder().feature(new ConditiaFeature()).build().mapper();
		Audiences audiences = new Audiences(Map.of(
				"operator", requires("- permission: MANAGE_SERVER"),
				"moderator", requires("- any:\n    - audience: operator\n    - permission: MODERATE_MEMBERS")
		));
		ObjectProvider<Audiences> provider = mock(ObjectProvider.class);
		when(provider.getObject()).thenReturn(audiences);
		permissions = new CommandDefaultPermissions(provider);
	}

	@Test
	void anOperatorsCommand_AsksForManageServer() throws Exception {
		assertEquals(Optional.of(EnumSet.of(Permission.MANAGE_SERVER)), permissions.of(command("- audience: operator")));
	}

	@Test
	void permissionsAndOtherLines_AskForThePermissionsOnly() throws Exception {
		assertEquals(Optional.of(EnumSet.of(Permission.BAN_MEMBERS, Permission.KICK_MEMBERS)),
				permissions.of(command("- permission: [ban_members, kick_members]\n- scope: guild")));
	}

	@Test
	void aModeratorsCommand_IsLeftToYui() throws Exception {
		assertEquals(Optional.empty(), permissions.of(command("- audience: moderator\n- scope: guild")));
	}

	@Test
	void aCommandForEveryone_AsksForNothing() {
		assertEquals(Optional.empty(), permissions.of(definition(Requirements.none())));
	}

	private CommandDefinition command(String yaml) throws Exception {
		return definition(requires(yaml));
	}

	private Requirements requires(String yaml) throws Exception {
		return mapper.readValue(yaml, Requirements.class);
	}

	private static CommandDefinition definition(Requirements requires) {
		return new CommandDefinition(true, List.of("test"), "{alias}", CommandCategory.NONE,
				new CommandCooldown(false, Duration.ofSeconds(0), null), requires, false);
	}
}
