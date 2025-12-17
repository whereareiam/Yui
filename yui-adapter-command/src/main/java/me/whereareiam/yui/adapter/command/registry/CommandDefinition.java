package me.whereareiam.yui.adapter.command.registry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.model.command.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.lang.reflect.Method;

@Slf4j
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CommandDefinition {
	private final String commandName;
	private final Command commandConfig;
	private Object beanInstance;
	private Method method;

	/**
	 * Invokes the method associated with this command definition on its bean.
	 */
	public void invoke(SlashCommandInteractionEvent event) {
		try {
			method.invoke(beanInstance, event);
		} catch (Exception e) {
			log.error("Error invoking command method: {} in '{}'", method.getName(), commandName, e);
		}
	}
}
