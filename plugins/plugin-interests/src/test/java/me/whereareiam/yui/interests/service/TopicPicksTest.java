package me.whereareiam.yui.interests.service;

import me.whereareiam.yui.api.role.FactRoles;
import me.whereareiam.yui.interests.entity.TopicPickEntity;
import me.whereareiam.yui.interests.model.Topic;
import me.whereareiam.yui.interests.model.Topics;
import me.whereareiam.yui.interests.repository.TopicPickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TopicPicksTest {
	private static final long MEMBER = 42L;

	private TopicPickRepository repository;
	private FactRoles roles;
	private TopicPicks picks;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		repository = mock(TopicPickRepository.class);
		roles = mock(FactRoles.class);
		Topics topics = new Topics(Map.of("development", topic("1"), "minecraft", topic("2")));
		ObjectProvider<Topics> provider = mock(ObjectProvider.class);
		when(provider.getObject()).thenReturn(topics);

		picks = new TopicPicks(repository, roles, provider);
	}

	@Test
	void picking_GivesTheNewAndTakesTheDroppedRoles() {
		when(repository.findByUserId(MEMBER)).thenReturn(List.of(new TopicPickEntity(MEMBER, "minecraft")));

		picks.pick(MEMBER, List.of("development"));

		verify(repository).save(any(TopicPickEntity.class));
		verify(repository).deleteById(new TopicPickEntity.Key(MEMBER, "minecraft"));
		verify(roles).hold(MEMBER, "1", true, "topic development");
		verify(roles).hold(MEMBER, "2", false, "topic minecraft");
	}

	@Test
	void aTopicTheFileDoesNotOffer_IsNotKept() {
		picks.pick(MEMBER, List.of("cooking"));

		verify(repository, never()).save(any());
		verify(roles, never()).hold(anyLong(), anyString(), anyBoolean(), anyString());
	}

	private static Topic topic(String role) {
		Topic topic = new Topic();
		topic.setRole(role);

		return topic;
	}

	@Test
	void ofAMember_AreTheirStoredPicks() {
		when(repository.findByUserId(MEMBER)).thenReturn(List.of(new TopicPickEntity(MEMBER, "development")));

		assertEquals(Set.of("development"), picks.of(MEMBER));
	}
}
