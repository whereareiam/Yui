package me.whereareiam.yui.interests.command;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.annotation.command.Command;
import me.whereareiam.yui.api.annotation.command.Definition;
import me.whereareiam.yui.api.command.Interaction;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.model.component.ComponentAttributes;
import me.whereareiam.yui.api.model.fluctlight.Fluctlight;
import me.whereareiam.yui.api.util.style.StyleKit;
import me.whereareiam.yui.interests.service.TopicMenu;
import me.whereareiam.yui.interests.service.TopicPicks;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import org.springframework.stereotype.Component;

/**
 * {@code /topics}: the member picks their topics again, in the menu onboarding showed them.
 */
@Component
@RequiredArgsConstructor
public class TopicsCommand {
	private final TopicPicks picks;
	private final TopicMenu menu;

	@Definition("topics")
	@Command("topics")
	public void onCommand(Interaction interaction) {
		Fluctlight member = interaction.fluctlight();
		ComponentAttributes attributes = ComponentAttributes.builder().put(TopicMenu.COMMAND, true).build();

		interaction.replyCallback()
				.replyEmbeds(StyleKit.embeds().primary()
						.setTitle(Translatable.text("interests.commands.topics.menu.title").resolve(member))
						.setDescription(Translatable.text("interests.commands.topics.menu.description").resolve(member))
						.build())
				.addComponents(ActionRow.of(menu.menu(member, picks.of(member.getId()), attributes)))
				.setEphemeral(true)
				.queue();
	}
}
