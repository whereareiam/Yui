package me.whereareiam.yue.adapter.command.registry;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.whereareiam.yue.api.model.command.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@Getter
@Setter
@ToString
public class CommandDefinition {
	private static final Logger logger = LoggerFactory.getLogger(CommandDefinition.class);

	private final String commandName;
	private final Command commandConfig;
	private Object beanInstance;
	private Method method;

	public CommandDefinition(String commandName, Command commandConfig) {
		this.commandName = commandName;
		this.commandConfig = commandConfig;
	}

	/**
	 * Invokes the method associated with this command definition on its bean.
	 */
	public void invoke(SlashCommandInteractionEvent event) {
		try {
			method.invoke(beanInstance, event);
		} catch (Exception e) {
			logger.error("Error invoking command method: {} in '{}'", method.getName(), commandName, e);
		}
	}
}
