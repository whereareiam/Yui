package me.whereareiam.yue.adapter.command.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import me.whereareiam.yue.api.model.command.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.lang.reflect.Method;

@Getter
@ToString
@AllArgsConstructor
public class CommandDefinition {
	private final String commandName;
	private final Command commandConfig;
	private final Object beanInstance;
	private final Method method;

	public void invoke(SlashCommandInteractionEvent event) {
		try {
			System.out.println("Invoking command: " + commandName);
			method.invoke(beanInstance, event);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
