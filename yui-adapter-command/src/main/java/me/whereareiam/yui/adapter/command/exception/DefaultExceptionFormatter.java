package me.whereareiam.yui.adapter.command.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.whereareiam.yui.command.exception.ExceptionResponse;
import me.whereareiam.yui.exception.command.base.CommandException;
import me.whereareiam.yui.command.Interaction;
import me.whereareiam.yui.translation.Translatable;
import me.whereareiam.yui.util.style.StyleKit;
import net.dv8tion.jda.api.EmbedBuilder;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.ReplySetting;
import org.incendo.cloud.discord.slash.DiscordSetting;
import org.incendo.cloud.exception.*;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.incendo.cloud.services.PipelineException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Formats default cloud exceptions using StyleKit embeds and translations.
 * <p>
 * This formatter replaces the default cloud-jda6 exception handlers with
 * custom handlers that send styled embeds instead of plain messages.
 */
@Slf4j
@RequiredArgsConstructor
public final class DefaultExceptionFormatter {
	private final JDA6CommandManager<Interaction> commandManager;

	/**
	 * Creates an exception handler for {@link NoSuchCommandException}.
	 */
	@NotNull
	public ExceptionHandler<Interaction, NoSuchCommandException> noSuchCommandHandler() {
		return context -> {
			CommandContext<Interaction> commandContext = context.context();
			handleWithDefer(commandContext, () -> {
				Interaction interaction = commandContext.sender();
				long userId = interaction.fluctlight().getId();
				String title = Translatable.text("error.command.notFound.title").resolve(userId);
				String description = Translatable.text("error.command.notFound.description").resolve(userId);
				
				return StyleKit.embeds().error()
						.setTitle(title)
						.setDescription(description);
			});
		};
	}
	
	/**
	 * Creates an exception handler for {@link InvalidSyntaxException}.
	 */
	@NotNull
	public ExceptionHandler<Interaction, InvalidSyntaxException> invalidSyntaxHandler() {
		return context -> {
			CommandContext<Interaction> commandContext = context.context();
			String correctSyntax = context.exception().correctSyntax();
			handleWithDefer(commandContext, () -> {
				Interaction interaction = commandContext.sender();
				long userId = interaction.fluctlight().getId();
				String title = Translatable.text("error.syntax.invalid.title").resolve(userId);
				String description = Translatable.text("error.syntax.invalid.description").with("value", correctSyntax).resolve(userId);
				
				return StyleKit.embeds().error()
						.setTitle(title)
						.setDescription(description);
			});
		};
	}
	
	/**
	 * Creates an exception handler for {@link ArgumentParseException}.
	 */
	@NotNull
	public ExceptionHandler<Interaction, ArgumentParseException> argumentParseHandler() {
		return context -> {
			CommandContext<Interaction> commandContext = context.context();
			Throwable cause = context.exception().getCause();
			String errorMessage = cause.getMessage();
			handleWithDefer(commandContext, () -> {
				Interaction interaction = commandContext.sender();
				long userId = interaction.fluctlight().getId();
				String title = Translatable.text("error.argument.parse.title").resolve(userId);
				String description = Translatable.text("error.argument.parse.description")
					.with("value", errorMessage)
					.resolve(userId);
				
				return StyleKit.embeds().error()
						.setTitle(title)
						.setDescription(description);
			});
		};
	}
	
	/**
	 * Creates an exception handler for {@link NoPermissionException}.
	 */
	@NotNull
	public ExceptionHandler<Interaction, NoPermissionException> noPermissionHandler() {
		return context -> {
			CommandContext<Interaction> commandContext = context.context();
			String permission = context.exception().permissionResult().permission().permissionString();
			handleWithDefer(commandContext, () -> {
				Interaction interaction = commandContext.sender();
				long userId = interaction.fluctlight().getId();
				String title = Translatable.text("error.permission.denied.title").resolve(userId);
				String description = Translatable.text("error.permission.denied.description").with("value", permission).resolve(userId);
				
				return StyleKit.embeds().error()
						.setTitle(title)
						.setDescription(description);
			});
		};
	}
	
	/**
	 * Creates an exception handler for {@link InvalidCommandSenderException}.
	 */
	@NotNull
	public ExceptionHandler<Interaction, InvalidCommandSenderException> invalidSenderHandler() {
		return context -> {
			CommandContext<Interaction> commandContext = context.context();
			handleWithDefer(commandContext, () -> {
				Interaction interaction = commandContext.sender();
				long userId = interaction.fluctlight().getId();
				String title = Translatable.text("error.sender.invalid.title").resolve(userId);
				String description = Translatable.text("error.sender.invalid.description").resolve(userId);
				
				return StyleKit.embeds().error()
						.setTitle(title)
						.setDescription(description);
			});
		};
	}

	/**
	 * Creates an exception handler for {@link PipelineException}.
	 * <p>
	 * PipelineException wraps exceptions thrown during pipeline execution. If the cause is a
	 * {@link CommandException}, it will be unwrapped and handled by the CommandException handler.
	 * Otherwise, it is treated as an unexpected exception and logged.
	 */
	@NotNull
	public ExceptionHandler<Interaction, PipelineException> pipelineExceptionHandler(
			@NotNull DefaultExceptionHandlerRegistry exceptionHandlerRegistry
	) {
		return context -> {
			CommandContext<Interaction> commandContext = context.context();
			PipelineException pipelineException = context.exception();
			Throwable cause = pipelineException.getCause();
			
			// If the cause is a CommandException, unwrap it and handle it through the CommandException handler
			if (cause instanceof CommandException commandException) {
				// Use the registry's handler logic to handle the unwrapped CommandException
				// We need to defer first, then handle in the callback
				Interaction interaction = commandContext.sender();
				boolean ephemeralErrors = commandManager.discordSettings().get(DiscordSetting.EPHEMERAL_ERROR_MESSAGES);
				
				var replyCallback = interaction.replyCallback();
				var interactionEvent = interaction.interactionEvent();
				if (replyCallback != null && interactionEvent != null) {
					replyCallback.deferReply(ephemeralErrors).queue(_ -> {
						// Try to find a handler for this exact exception type
						Class<? extends CommandException> exceptionClass = commandException.getClass();
						Function<CommandException, ExceptionResponse> handler =
								exceptionHandlerRegistry.getHandler(exceptionClass);
						
						// If not found, try to find a handler for a superclass
						if (handler == null) {
							Class<?> currentClass = exceptionClass.getSuperclass();
							while (currentClass != null && CommandException.class.isAssignableFrom(currentClass)) {
								@SuppressWarnings("unchecked")
								Class<? extends CommandException> superClass = (Class<? extends CommandException>) currentClass;
								handler = exceptionHandlerRegistry.getHandler(superClass);
								if (handler != null) break;

								currentClass = currentClass.getSuperclass();
							}
						}
						
						// If we found a handler, use it; otherwise, use the exception's createResponse method
						ExceptionResponse response;
						if (handler != null) {
							response = handler.apply(commandException);
						} else {
							response = commandException.createResponse(interaction);
						}
						
						// Send the response via hook
						exceptionHandlerRegistry.sendResponseDeferred(commandContext, response, ephemeralErrors);
					});
					return;
				}
				
				// Fallback if defer failed
				ExceptionResponse response = commandException.createResponse(interaction);
				exceptionHandlerRegistry.sendResponse(commandContext, response);
				return;
			}
			
			// If it's not a CommandException, treat it as an unexpected exception
			// Log it and show a generic error message
			log.error("Unexpected exception wrapped in PipelineException", pipelineException);
			
			Throwable exceptionToShow = cause != null ? cause : pipelineException;
			String errorMessage = exceptionToShow.getMessage();
			if (errorMessage == null || errorMessage.isEmpty())
				errorMessage = "An unexpected error occurred";
			
			String finalErrorMessage = errorMessage;
			handleWithDefer(commandContext, () -> {
				Interaction interaction = commandContext.sender();
				long userId = interaction.fluctlight().getId();
				String title = Translatable.text("error.unexpected.title").resolve(userId);
				String description = Translatable.text("error.unexpected.description").with("value", finalErrorMessage).resolve(userId);
				
				return StyleKit.embeds().error()
						.setTitle(title)
						.setDescription(description);
			});
		};
	}

	/**
	 * Creates a catch-all exception handler for any unhandled exceptions.
	 * <p>
	 * This handler will only be used if no more specific handler is registered for the exception type.
	 * Registered handlers (including custom CommandException handlers) take precedence.
	 * <p>
	 * Unexpected exceptions are logged to the console for debugging purposes.
	 */
	@NotNull
	public ExceptionHandler<Interaction, Throwable> catchAllHandler() {
		return context -> {
			CommandContext<Interaction> commandContext = context.context();
			Throwable exception = context.exception();
			
			// Log unexpected exception to console for debugging
			log.error("Unexpected exception occurred during command execution", exception);
			
			Throwable cause = exception.getCause();
			String errorMessage = cause != null ? cause.getMessage() : exception.getMessage();
			if (errorMessage == null || errorMessage.isEmpty())
				errorMessage = "An unexpected error occurred";
			
			String finalErrorMessage = errorMessage;
			handleWithDefer(commandContext, () -> {
				Interaction interaction = commandContext.sender();
				long userId = interaction.fluctlight().getId();
				String title = Translatable.text("error.unexpected.title").resolve(userId);
				String description = Translatable.text("error.unexpected.description").with("value", finalErrorMessage).resolve(userId);
				
				return StyleKit.embeds().error()
						.setTitle(title)
						.setDescription(description);
			});
		};
	}
	
	/**
	 * Handles an exception by deferring immediately (if possible) and then creating/sending the embed.
	 * <p>
	 * This ensures we respond to Discord within 3 seconds to avoid "application did not respond" errors.
	 *
	 * @param context the command context
	 * @param embedSupplier function that creates the embed (called after defer if possible)
	 */
	private void handleWithDefer(
			@NotNull CommandContext<Interaction> context,
			@NotNull Supplier<EmbedBuilder> embedSupplier
	) {
		Interaction interaction = context.sender();
		boolean ephemeralErrors = commandManager.discordSettings().get(DiscordSetting.EPHEMERAL_ERROR_MESSAGES);
		
		// Try to defer immediately to avoid timeout
		if (interaction.replyCallback() != null && interaction.interactionEvent() != null) {
			var replyCallback = interaction.replyCallback();
			var interactionEvent = interaction.interactionEvent();
			if (replyCallback != null && interactionEvent != null) {
				replyCallback.deferReply(ephemeralErrors).queue(_ -> {
					EmbedBuilder embed = embedSupplier.get();
					var hook = interactionEvent.getHook();
					hook.sendMessageEmbeds(embed.build()).queue();
				});

				return;
			}
		}
		
		// Fallback: create embed and send directly
		EmbedBuilder embed = embedSupplier.get();
		sendEmbed(context, embed);
	}

	/**
	 * Sends an embed response.
	 * <p>
	 * For exceptions, we always defer immediately to avoid Discord's "application did not respond" error,
	 * then send via the hook.
	 */
	private void sendEmbed(@NotNull CommandContext<Interaction> context, @NotNull EmbedBuilder embed) {
		Interaction interaction = context.sender();
		
		// Check ReplySetting to determine if we should use hook or direct reply
		ReplySetting<?> replySetting = context.getOrDefault(JDA6CommandManager.META_REPLY_SETTING, null);
		
		// Check if ephemeral errors are enabled
		boolean ephemeralErrors = commandManager.discordSettings().get(DiscordSetting.EPHEMERAL_ERROR_MESSAGES);
		
		// Already deferred, use hook
		if (replySetting != null && replySetting.defer()) {
			var interactionEvent = interaction.interactionEvent();
			if (interactionEvent != null) {
				var hook = interactionEvent.getHook();
				hook.sendMessageEmbeds(embed.build())
						.setEphemeral(ephemeralErrors)
						.queue();
			}
			return;
		}
		
		// For exceptions, always defer immediately to avoid timeout, then send via hook
		var replyCallback = interaction.replyCallback();
		var interactionEvent = interaction.interactionEvent();
		if (replyCallback != null && interactionEvent != null) {
			replyCallback.deferReply(ephemeralErrors).queue(_ -> {
				var hook = interactionEvent.getHook();
				hook.sendMessageEmbeds(embed.build()).queue();
			});

			return;
		}
		
		// Direct reply via callback (fallback)
		if (replyCallback != null) {
			replyCallback.replyEmbeds(embed.build())
					.setEphemeral(ephemeralErrors)
					.queue();
			return;
		}
		
		// Direct reply via event (fallback)
		if (interactionEvent != null) {
			interactionEvent.replyEmbeds(embed.build())
					.setEphemeral(ephemeralErrors)
					.queue();
		}
	}
}


