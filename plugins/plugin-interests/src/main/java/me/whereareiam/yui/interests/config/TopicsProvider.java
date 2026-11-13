package me.whereareiam.yui.interests.config;

import me.whereareiam.yui.api.config.PluginConfigProvider;
import me.whereareiam.yui.interests.model.Topic;
import me.whereareiam.yui.interests.model.Topics;
import org.springframework.stereotype.Component;

/**
 * Reads {@code topics.yml}; without one there are no topics and the step is skipped.
 */
@Component
public class TopicsProvider extends PluginConfigProvider<Topics> {
	@Override
	protected Topics load() {
		return new Topics(readMap("topics", Topic.class));
	}

	@Override
	public Class<Topics> getObjectType() {
		return Topics.class;
	}
}
