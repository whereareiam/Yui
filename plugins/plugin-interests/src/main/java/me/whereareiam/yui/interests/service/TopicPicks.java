package me.whereareiam.yui.interests.service;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.role.FactRoles;
import me.whereareiam.yui.interests.entity.TopicPickEntity;
import me.whereareiam.yui.interests.model.Topic;
import me.whereareiam.yui.interests.model.Topics;
import me.whereareiam.yui.interests.repository.TopicPickRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Owns the topics each member picked: keeps them in {@code interests_picks} and makes each topic's role follow them
 * through core's {@link FactRoles}.
 */
@Component
@RequiredArgsConstructor
public class TopicPicks {
	private final TopicPickRepository picks;
	private final FactRoles roles;
	private final ObjectProvider<Topics> topics;

	/**
	 * @return the ids of the topics the member picked
	 */
	public Set<String> of(long member) {
		return picks.findByUserId(member).stream().map(TopicPickEntity::getTopic).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Makes the member's picks the given topics, of those {@code topics.yml} offers: the others are dropped.
	 */
	public void pick(long member, Collection<String> picked) {
		Set<String> wanted = picked.stream().filter(topics.getObject().all()::containsKey).collect(Collectors.toSet());
		Set<String> had = of(member);

		for (String topic : had) {
			if (wanted.contains(topic)) continue;

			picks.deleteById(new TopicPickEntity.Key(member, topic));
			hold(member, topic, false);
		}
		for (String topic : wanted) {
			if (had.contains(topic)) continue;

			picks.save(new TopicPickEntity(member, topic));
			hold(member, topic, true);
		}
	}

	/**
	 * Gives a member the roles of their picks again, such as one who rejoined.
	 */
	public void restore(long member) {
		of(member).forEach(topic -> hold(member, topic, true));
	}

	/**
	 * Forgets the member's picks and takes their roles.
	 */
	public void forget(long member) {
		pick(member, Set.of());
	}

	private void hold(long member, String topic, boolean holds) {
		Topic offered = topics.getObject().all().get(topic);
		if (offered == null || offered.getRole() == null || offered.getRole().isBlank()) return;

		roles.hold(member, offered.getRole(), holds, "topic " + topic);
	}
}
