package me.whereareiam.yui.exception.command.base;

import me.whereareiam.yui.command.exception.ExceptionResponse;
import me.whereareiam.yui.command.exception.ExceptionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Base exception class for custom command exceptions in Yui.
 * <p>
 * Custom exceptions should extend this class and implement {@link #createResponse(ExceptionContext)}
 * to define what message or embed should be sent when the exception occurs.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class RequirementFailedException extends CommandException {
 *     public RequirementFailedException() {
 *         super();
 *     }
 *     
 *     @Override
 *     public ExceptionResponse createResponse(ExceptionContext context) {
 *         long userId = context.getUserId();
 *         String message = Translatable.forUser("error.requirement.failed", userId);
 *         return ExceptionResponse.message(message);
 *     }
 * }
 * }</pre>
 */
@SuppressWarnings("unused")
public abstract class CommandException extends RuntimeException {
	/**
	 * Creates a new command exception.
	 */
	protected CommandException() {
		super();
	}

	/**
	 * Creates a new command exception with a message.
	 *
	 * @param message The exception message
	 */
	protected CommandException(@NotNull String message) {
		super(message);
	}
	
	/**
	 * Creates a new command exception with a message and cause.
	 *
	 * @param message The exception message
	 * @param cause   The cause
	 */
	protected CommandException(@NotNull String message, @NotNull Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Creates a new command exception with a cause.
	 *
	 * @param cause The cause
	 */
	protected CommandException(@NotNull Throwable cause) {
		super(cause);
	}
	
	/**
	 * Creates the response that should be sent when this exception occurs.
	 * <p>
	 * This method is called by the exception handler system to determine
	 * what message or embed to send to the fluctlight.
	 *
	 * @param context The exception context providing fluctlight and command information
	 * @return The response to send (message or embed)
	 */
	@NotNull
	public abstract ExceptionResponse createResponse(@NotNull ExceptionContext context);
}
