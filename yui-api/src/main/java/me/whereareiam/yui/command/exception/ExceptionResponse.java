package me.whereareiam.yui.command.exception;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a response to send when an exception occurs.
 * <p>
 * This can be either a simple text message or an embed.
 */
public sealed interface ExceptionResponse permits ExceptionResponse.MessageResponse, ExceptionResponse.EmbedResponse {
	/**
	 * Creates a simple message response.
	 *
	 * @param message The message to send
	 * @return A message response
	 */
	@NotNull
	static ExceptionResponse message(@NotNull String message) {
		return new MessageResponse(message);
	}
	
	/**
	 * Creates an embed response.
	 *
	 * @param embedBuilder The embed builder to send
	 * @return An embed response
	 */
	@NotNull
	static ExceptionResponse embed(@NotNull EmbedBuilder embedBuilder) {
		return new EmbedResponse(embedBuilder);
	}
	
	/**
	 * Simple message response.
	 */
	record MessageResponse(@NotNull String message) implements ExceptionResponse {
	}
	
	/**
	 * Embed response.
	 */
	record EmbedResponse(@NotNull EmbedBuilder embedBuilder) implements ExceptionResponse {
	}
}
