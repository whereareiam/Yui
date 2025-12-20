package me.whereareiam.yui.command.exception;

/**
 * Context information available when handling a command exception.
 * <p>
 * This abstraction provides access to fluctlight information without exposing
 * platform-specific or framework-specific types.
 */
public interface ExceptionContext {
	/**
	 * Returns the ID of the fluctlight who triggered the command.
	 *
	 * @return The fluctlight ID
	 */
	long getUserId();
	
	/**
	 * Returns the ID of the channel where the command was executed.
	 *
	 * @return The channel ID, or 0 if not available
	 */
	long getChannelId();
}
