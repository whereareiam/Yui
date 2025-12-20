package me.whereareiam.yui.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.model.fluctlight.Fluctlight;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Framework-agnostic interaction wrapper that uses Fluctlight instead of User.
 * <p>
 * This class mirrors the structure of Cloud's JDAInteraction but uses Fluctlight,
 * making our framework independent of Cloud while maintaining the same API structure.
 */
@Getter
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class Interaction {
	private final Fluctlight fluctlight;
	private final Guild guild;
	private final GenericCommandInteractionEvent interactionEvent;
	private final IReplyCallback replyCallback;
	private final List<OptionMapping> optionMappings;

	/**
	 * Returns the Fluctlight that triggered the interaction.
	 *
	 * @return the Fluctlight
	 */
	public Fluctlight fluctlight() {
		return fluctlight;
	}

	/**
	 * Returns the guild that triggered the interaction, if the interaction took place in a guild.
	 *
	 * @return the guild, or {@code null}
	 */
	public Guild guild() {
		return guild;
	}

	/**
	 * Returns the interaction event that triggered the command, if relevant.
	 *
	 * @return the interaction event, or {@code null}
	 */
	public GenericCommandInteractionEvent interactionEvent() {
		return interactionEvent;
	}

	/**
	 * Returns the reply callback, if relevant.
	 *
	 * @return the reply callback, or {@code null}
	 */
	public IReplyCallback replyCallback() {
		return replyCallback;
	}

	/**
	 * Returns the raw JDA option mappings.
	 *
	 * @return option mappings
	 */
	public List<OptionMapping> optionMappings() {
		return optionMappings;
	}

	/**
	 * Returns the option mapping with the given {@code key}, if it exists.
	 *
	 * @param key mapping key
	 * @return the mapping
	 */
	public Optional<OptionMapping> getOptionMapping(final String key) {
		Objects.requireNonNull(key, "key");
		return this.optionMappings().stream()
				.filter(mapping -> mapping.getName().equalsIgnoreCase(key))
				.findFirst();
	}
}

