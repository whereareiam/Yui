package me.whereareiam.yui.adapter.command.exception;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.command.exception.ExceptionContext;
import me.whereareiam.yui.command.exception.ExceptionResponse;
import me.whereareiam.yui.exception.command.base.CommandException;
import net.dv8tion.jda.api.EmbedBuilder;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.discord.jda6.JDA6CommandManager;
import org.incendo.cloud.discord.jda6.JDAInteraction;
import org.incendo.cloud.discord.jda6.ReplySetting;
import org.incendo.cloud.discord.slash.DiscordSetting;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Internal registry for custom exception handlers.
 * <p>
 * This is used internally by the command system. Users should register
 * exception handlers through {@link me.whereareiam.yui.command.CommandService#registerExceptionHandler}.
 */
@Component
@RequiredArgsConstructor
public final class DefaultExceptionHandlerRegistry {
	private final org.incendo.cloud.discord.jda6.JDA6CommandManager<JDAInteraction> commandManager;

	private final Map<Class<? extends CommandException>, Function<CommandException, ExceptionResponse>> handlers = new HashMap<>();

	/**
	 * Registers a handler for a specific exception type.
	 * <p>
	 * This is called internally by {@link me.whereareiam.yui.command.CommandService}.
	 *
	 * @param exceptionType The exception type
	 * @param handler       The handler function that creates a response
	 * @param <T>           The exception type
	 */
	@SuppressWarnings("unchecked")
	public <T extends CommandException> void register(
			@NotNull Class<T> exceptionType,
			@NotNull Function<T, ExceptionResponse> handler
	) {
		handlers.put(exceptionType, (Function<CommandException, ExceptionResponse>) handler);
	}
	
	/**
	 * Creates a cloud exception handler that uses this registry.
	 * <p>
	 * This method bridges between Cloud's exception handling system and Yui's exception system.
	 *
	 * @return The cloud exception handler
	 */
	@NotNull
	public ExceptionHandler<JDAInteraction, CommandException> createCloudHandler() {
		return context -> {
			CommandException exception = context.exception();
			CommandContext<JDAInteraction> commandContext = context.context();
			JDAInteraction interaction = commandContext.sender();
			
			// Defer immediately to avoid timeout, then do the work in the callback
			boolean ephemeralErrors = commandManager.discordSettings().get(DiscordSetting.EPHEMERAL_ERROR_MESSAGES);
			var replyCallback = interaction.replyCallback();
			var interactionEvent = interaction.interactionEvent();
			if (replyCallback != null && interactionEvent != null) {
				replyCallback.deferReply(ephemeralErrors).queue(_ -> {
					// Create the API-level exception context
					ExceptionContext exceptionContext = new CloudExceptionContext(commandContext);
					
					// Try to find a handler for this exact exception type
					Class<? extends CommandException> exceptionClass = exception.getClass();
					Function<CommandException, ExceptionResponse> handler = getHandler(exceptionClass);
					
					// If not found, try to find a handler for a superclass
					if (handler == null) {
						Class<?> currentClass = exceptionClass.getSuperclass();
						while (currentClass != null && CommandException.class.isAssignableFrom(currentClass)) {
							@SuppressWarnings("unchecked")
							Class<? extends CommandException> superClass = (Class<? extends CommandException>) currentClass;
							handler = getHandler(superClass);
							if (handler != null) {
								break;
							}
							currentClass = currentClass.getSuperclass();
						}
					}
					
					// If we found a handler, use it; otherwise, use the exception's createResponse method
					ExceptionResponse response;
					if (handler != null) {
						response = handler.apply(exception);
					} else {
						response = exception.createResponse(exceptionContext);
					}
					
					// Send the response via hook
					sendResponseDeferred(commandContext, response, ephemeralErrors);
				});
				return;
			}
			
			// Fallback if defer failed - do work synchronously
			ExceptionContext exceptionContext = new CloudExceptionContext(commandContext);
			
			// Try to find a handler for this exact exception type
			Class<? extends CommandException> exceptionClass = exception.getClass();
			Function<CommandException, ExceptionResponse> handler = getHandler(exceptionClass);
			
			// If not found, try to find a handler for a superclass
			if (handler == null) {
				Class<?> currentClass = exceptionClass.getSuperclass();
				while (currentClass != null && CommandException.class.isAssignableFrom(currentClass)) {
					@SuppressWarnings("unchecked")
					Class<? extends CommandException> superClass = (Class<? extends CommandException>) currentClass;
					handler = getHandler(superClass);
					if (handler != null) {
						break;
					}
					currentClass = currentClass.getSuperclass();
				}
			}
			
			// If we found a handler, use it; otherwise, use the exception's createResponse method
			ExceptionResponse response;
			if (handler != null) {
				response = handler.apply(exception);
			} else {
				response = exception.createResponse(exceptionContext);
			}
			
			// Send the response
			sendResponse(commandContext, response);
		};
	}
	
	/**
	 * Gets the handler for the given exception type, or null if not found.
	 * <p>
	 * This is package-private so DefaultExceptionFormatter can use it for PipelineException unwrapping.
	 *
	 * @param exceptionType The exception type
	 * @return The handler function, or null
	 */
	@Nullable
	Function<CommandException, ExceptionResponse> getHandler(@NotNull Class<? extends CommandException> exceptionType) {
		return handlers.get(exceptionType);
	}
	
	/**
	 * Sends the response to the user when already deferred.
	 * <p>
	 * This is package-private so DefaultExceptionFormatter can use it for PipelineException unwrapping.
	 */
	void sendResponseDeferred(
			@NotNull CommandContext<JDAInteraction> context,
			@NotNull ExceptionResponse response,
			boolean ephemeralErrors
	) {
		JDAInteraction interaction = context.sender();
		var interactionEvent = interaction.interactionEvent();
		
		if (interactionEvent == null) {
			// Fallback if no event - use the regular sendResponse
			sendResponseInternal(context, response);
			return;
		}
		
		var hook = interactionEvent.getHook();
		if (response instanceof ExceptionResponse.MessageResponse(String message)) {
			hook.sendMessage(message)
					.setEphemeral(ephemeralErrors)
					.queue();
			return;
		}

		if (response instanceof ExceptionResponse.EmbedResponse(EmbedBuilder embedBuilder)) {
			hook.sendMessageEmbeds(embedBuilder.build())
					.setEphemeral(ephemeralErrors)
					.queue();
		}
	}

	/**
	 * Sends the response to the user.
	 * <p>
	 * This is package-private so DefaultExceptionFormatter can use it for PipelineException unwrapping.
	 */
	void sendResponse(@NotNull CommandContext<JDAInteraction> context, @NotNull ExceptionResponse response) {
		sendResponseInternal(context, response);
	}

	/**
	 * Internal method to send the response to the user.
	 */
	private void sendResponseInternal(@NotNull CommandContext<JDAInteraction> context, @NotNull ExceptionResponse response) {
		JDAInteraction interaction = context.sender();
		
		// Check if we need to defer first (similar to cloud-jda6's default handlers)
		ReplySetting<?> replySetting =
				context.getOrDefault(JDA6CommandManager.META_REPLY_SETTING, null);
		
		// Check if ephemeral errors are enabled
		boolean ephemeralErrors = commandManager.discordSettings().get(DiscordSetting.EPHEMERAL_ERROR_MESSAGES);
		
		if (response instanceof ExceptionResponse.MessageResponse(String message)) {
			sendMessage(interaction, message, replySetting, ephemeralErrors);
			return;
		}

		if (response instanceof ExceptionResponse.EmbedResponse(EmbedBuilder embedBuilder))
			sendEmbed(interaction, embedBuilder, replySetting, ephemeralErrors);
	}
	
	/**
	 * Sends a message response.
	 * <p>
	 * For exceptions, we always defer immediately to avoid Discord's "application did not respond" error,
	 * then send via the hook.
	 */
	private void sendMessage(
			@NotNull JDAInteraction interaction, 
			@NotNull String message,
			@Nullable ReplySetting<?> replySetting,
			boolean ephemeralErrors
	) {
		// Already deferred, use hook
		if (replySetting != null && replySetting.defer()) {
			var interactionEvent = interaction.interactionEvent();
			if (interactionEvent != null) {
				var hook = interactionEvent.getHook();
				hook.sendMessage(message)
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
				hook.sendMessage(message).queue();
			});
			return;
		}

		// Direct reply via callback (fallback)
		if (replyCallback != null) {
			replyCallback.reply(message)
					.setEphemeral(ephemeralErrors)
					.queue();
			return;
		}

		// Direct reply via event (fallback)
		if (interactionEvent != null) {
			interactionEvent.reply(message)
					.setEphemeral(ephemeralErrors)
					.queue();
		}
	}
	
	/**
	 * Sends an embed response.
	 * <p>
	 * For exceptions, we always defer immediately to avoid Discord's "application did not respond" error,
	 * then send it via the hook.
	 */
	private void sendEmbed(
			@NotNull JDAInteraction interaction, 
			@NotNull EmbedBuilder embedBuilder,
			@Nullable ReplySetting<?> replySetting,
			boolean ephemeralErrors
	) {
		// Already deferred, use hook
		if (replySetting != null && replySetting.defer()) {
			var interactionEvent = interaction.interactionEvent();
			if (interactionEvent != null) {
				var hook = interactionEvent.getHook();
				hook.sendMessageEmbeds(embedBuilder.build())
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
				hook.sendMessageEmbeds(embedBuilder.build()).queue();
			});
			return;
		}

		// Direct reply via callback (fallback)
		if (replyCallback != null) {
			replyCallback.replyEmbeds(embedBuilder.build())
					.setEphemeral(ephemeralErrors)
					.queue();
			return;
		}

		// Direct reply via event (fallback)
		if (interactionEvent != null) {
			interactionEvent.replyEmbeds(embedBuilder.build())
					.setEphemeral(ephemeralErrors)
					.queue();
		}
	}
}

