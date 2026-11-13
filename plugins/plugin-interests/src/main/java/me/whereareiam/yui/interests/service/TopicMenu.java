package me.whereareiam.yui.interests.service;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.Key;
import me.whereareiam.yui.api.model.component.ComponentAttributes;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.service.InteractionService;
import me.whereareiam.yui.interests.model.Topics;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * The menu members pick topics in, the same in onboarding and in {@code /topics}: every topic of {@code topics.yml}
 * with its name and description in the member's language, their picks already chosen, any number or none.
 */
@Component
@RequiredArgsConstructor
public class TopicMenu {
	/**
	 * The menu's path.
	 */
	public static final String PATH = "pick";

	/**
	 * Set on the menu of {@code /topics}, whose reply then says that the picks are saved.
	 */
	public static final Key<Boolean> COMMAND = Key.of("interests.command", Boolean.class);

	private static final int DESCRIPTION = 100;

	private final InteractionService interactions;
	private final ObjectProvider<Topics> topics;

	public StringSelectMenu menu(Fluctlight member, Set<String> picked, ComponentAttributes attributes) {
		List<SelectOption> options = topics.getObject().all().keySet().stream()
				.map(topic -> option(topic, member, picked.contains(topic)))
				.toList();

		return interactions.createStringSelectMenu(PATH, attributes)
				.setPlaceholder(Translatable.text("interests.journey.topics.placeholder").resolve(member))
				.setMinValues(0)
				.setMaxValues(Math.max(1, options.size()))
				.addOptions(options)
				.build();
	}

	private static SelectOption option(String topic, Fluctlight member, boolean picked) {
		String key = "interests.topics." + topic + ".";
		SelectOption option = SelectOption.of(text(key + "name", topic, member), topic).withDefault(picked);
		String description = text(key + "description", "", member);
		if (description.isBlank()) return option;

		return option.withDescription(description.length() > DESCRIPTION ? description.substring(0, DESCRIPTION) : description);
	}

	// A text the administrator did not write shows the fallback, not its key.
	private static String text(String key, String fallback, Fluctlight member) {
		String text = Translatable.text(key).resolve(member);

		return text.equals(key) ? fallback : text;
	}
}
