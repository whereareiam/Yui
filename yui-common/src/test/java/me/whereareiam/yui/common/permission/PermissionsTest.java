package me.whereareiam.yui.common.permission;

import me.whereareiam.conditia.Conditions;
import me.whereareiam.conditia.DeferredPlace;
import me.whereareiam.conditia.exception.ConditionException;
import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.conditia.model.Situation;
import me.whereareiam.conditia.result.Verdict;
import me.whereareiam.configura.Configura;
import me.whereareiam.yui.api.model.config.audience.Audiences;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.permission.Actor;
import me.whereareiam.yui.api.permission.PermissionParts;
import me.whereareiam.yui.api.type.ConfigurationType;
import me.whereareiam.yui.common.config.ConfiguraConfiguration;
import me.whereareiam.yui.common.config.template.AudiencesTemplate;
import me.whereareiam.yui.common.permission.type.interaction.ChannelTypeCondition;
import me.whereareiam.yui.common.permission.type.interaction.ScopeCondition;
import me.whereareiam.yui.common.permission.type.member.LanguageCondition;
import me.whereareiam.yui.common.permission.type.member.PermissionCondition;
import me.whereareiam.yui.common.permission.type.member.RoleCondition;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.Interaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A command's {@code requires:} as Yui binds and checks it: Conditia with Yui's core types and audiences.
 */
class PermissionsTest {
	private Configura configura;
	private Conditions conditions;

	@BeforeEach
	void setUp() {
		configura = ConfiguraConfiguration.build(ConfigurationType.YAML);
		conditions = new PermissionConfiguration().conditions(configura, List.of(
				new PermissionCondition(), new RoleCondition(), new LanguageCondition(), new ScopeCondition(), new ChannelTypeCondition()
		));
		Audiences audiences = new AudiencesTemplate(configura).supply(new Audiences());
		audiences.all().forEach(conditions::name);
	}

	@Test
	void clear_LetsAModeratorInAGuildTextChannel() throws IOException {
		DeferredPlace clear = place("""
				- audience: moderator
				- scope: guild
				- channel-type: [text, thread]
				""");

		assertTrue(check(clear, member(Permission.MODERATE_MEMBERS), ChannelType.TEXT).passed());
	}

	@Test
	void clear_StopsAMemberWithoutThePermission() throws IOException {
		Verdict verdict = check(place("- audience: moderator"), member(), ChannelType.TEXT);

		Verdict.Failed failed = assertInstanceOf(Verdict.Failed.class, verdict);
		assertTrue(failed.path().startsWith("commands.test.requires[0].audience"));
	}

	@Test
	void anOperator_IsAModeratorToo() throws IOException {
		assertTrue(check(place("- audience: moderator"), member(Permission.MANAGE_SERVER), ChannelType.TEXT).passed());
	}

	@Test
	void channelTypes_AreReadInAnyCase() throws IOException {
		assertTrue(check(place("- channel-type: THREAD"), member(), ChannelType.GUILD_PUBLIC_THREAD).passed());
	}

	@Test
	void language_AsksTheActorsLanguages() throws IOException {
		assertTrue(check(place("- language: [de, fr]"), member(), ChannelType.TEXT).passed());
	}

	@Test
	void anUnknownType_StopsBinding() {
		assertThrows(ConditionException.class, () -> place("- owns: product"));
	}

	private DeferredPlace place(String yaml) throws IOException {
		Requirements requires = configura.mapper().readValue(yaml, Requirements.class);

		return conditions.deferredPlace("commands.test.requires", requires, PermissionParts.ACTOR, PermissionParts.INTERACTION);
	}

	private static Verdict check(DeferredPlace place, Member member, ChannelType type) {
		Interaction interaction = mock(Interaction.class);
		MessageChannelUnion channel = mock(MessageChannelUnion.class);
		when(channel.getType()).thenReturn(type);
		when(interaction.getChannel()).thenReturn(channel);
		when(interaction.isFromGuild()).thenReturn(true);

		Fluctlight fluctlight = mock(Fluctlight.class);
		when(fluctlight.speaks(DiscordLocale.GERMAN)).thenReturn(true);
		Actor actor = new Actor(mock(User.class), member, fluctlight);

		return place.check(Situation.of(PermissionParts.ACTOR, actor).and(PermissionParts.INTERACTION, interaction))
				.toCompletableFuture().join();
	}

	private static Member member(Permission... permissions) {
		Member member = mock(Member.class);
		Set<Permission> held = Set.of(permissions);
		when(member.hasPermission(anyCollection())).thenAnswer(call -> held.containsAll(call.<Collection<Permission>>getArgument(0)));

		return member;
	}
}
