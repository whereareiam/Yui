package me.whereareiam.yui.interests.listener;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.annotation.ComponentListener;
import me.whereareiam.yui.api.message.Translatable;
import me.whereareiam.yui.api.service.InteractionService;
import me.whereareiam.yui.api.util.style.StyleKit;
import me.whereareiam.yui.interests.service.TopicMenu;
import me.whereareiam.yui.interests.service.TopicPicks;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.stereotype.Component;

/**
 * A member changed their picks in the topic menu: they are saved at once and the roles follow. In {@code /topics}
 * the reply then says so.
 */
@Component
@RequiredArgsConstructor
public class PickListener {
	private final InteractionService interactions;
	private final TopicPicks picks;

	@ComponentListener(TopicMenu.PATH)
	public void onPick(StringSelectInteractionEvent event) {
		event.deferEdit().queue();

		long member = event.getUser().getIdLong();
		picks.pick(member, event.getValues());

		if (!interactions.getAttributes(event).get(TopicMenu.COMMAND).orElse(false)) return;

		event.getHook().editOriginalEmbeds(StyleKit.embeds().success()
				.setTitle(Translatable.text("interests.commands.topics.saved.title").resolve(member))
				.setDescription(Translatable.text("interests.commands.topics.saved.description").resolve(member))
				.build()).queue();
	}
}
