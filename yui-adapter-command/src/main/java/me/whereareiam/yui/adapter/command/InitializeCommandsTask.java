package me.whereareiam.yui.adapter.command;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.LifecycleTask;
import me.whereareiam.yui.adapter.command.exception.DefaultExceptionFormatter;
import me.whereareiam.yui.adapter.command.exception.DefaultExceptionHandlerRegistry;
import me.whereareiam.yui.exception.command.base.CommandException;
import me.whereareiam.yui.Registry;
import me.whereareiam.yui.command.CommandService;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.NoSuchCommandException;
import org.incendo.cloud.services.PipelineException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InitializeCommandsTask implements LifecycleTask {
	private final Registry<LifecycleTask> lifecycleRegistry;
	private final CommandService commandService;
	private final ApplicationContext ctx;
	private final JDA6CommandManager<JDAInteraction> commandManager;
	private final DefaultExceptionHandlerRegistry exceptionHandlerRegistry;

	@PostConstruct
	public void registerSelf() {
		lifecycleRegistry.register(this);
	}

	@Override
	public String getName() {
		return "INIT_COMMANDS";
	}

	@Override
	public List<String> getDependencies() {
		return List.of("INIT_TRANSLATIONS");
	}

	@Override
	public CompletableFuture<Void> start() {
		// Initialize exception handlers before registering commands
		initializeExceptionHandlers();
		
		// Register commands
		commandService.register(ctx);
		
		return CompletableFuture.completedFuture(null);
	}

	private void initializeExceptionHandlers() {
		log.debug("Initializing exception handlers");

		// Clear default cloud-jda6 exception handlers
		commandManager.exceptionController().clearHandlers();

		// Create formatter with command manager
		DefaultExceptionFormatter formatter = new DefaultExceptionFormatter(commandManager);

		// Register custom handlers for default cloud exceptions
		commandManager.exceptionController().registerHandler(
				NoSuchCommandException.class,
				formatter.noSuchCommandHandler()
		);

		commandManager.exceptionController().registerHandler(
				InvalidSyntaxException.class,
				formatter.invalidSyntaxHandler()
		);

		commandManager.exceptionController().registerHandler(
				ArgumentParseException.class,
				formatter.argumentParseHandler()
		);

		commandManager.exceptionController().registerHandler(
				NoPermissionException.class,
				formatter.noPermissionHandler()
		);

		commandManager.exceptionController().registerHandler(
				InvalidCommandSenderException.class,
				formatter.invalidSenderHandler()
		);

		// Register handler for custom CommandException types
		commandManager.exceptionController().registerHandler(
				CommandException.class,
				exceptionHandlerRegistry.createCloudHandler()
		);

		// Register PipelineException handler to unwrap CommandException from it
		// This must be registered before the catch-all handler
		commandManager.exceptionController().registerHandler(
				PipelineException.class,
				formatter.pipelineExceptionHandler(exceptionHandlerRegistry)
		);

		// Register catch-all handler last - it will only be used if no specific handler matches
		commandManager.exceptionController().registerHandler(
				Throwable.class,
				formatter.catchAllHandler()
		);

		log.debug("Exception handlers initialized");
	}
}

