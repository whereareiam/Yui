package me.whereareiam.yue.adapter.command;

import jakarta.annotation.PostConstruct;
import me.whereareiam.yue.adapter.command.registrar.CommandRegistrar;
import me.whereareiam.yue.adapter.command.scanner.CommandScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandInitialization {
	private final CommandScanner commandScanner;
	private final CommandRegistrar commandRegistrar;

	@Autowired
	public CommandInitialization(CommandScanner commandScanner,
	                             CommandRegistrar commandRegistrar) {
		this.commandScanner = commandScanner;
		this.commandRegistrar = commandRegistrar;
	}

	@PostConstruct
	public void initCommands() {
		commandScanner.scanAnnotatedCommands();
		commandRegistrar.registerDiscordCommands();
	}
}