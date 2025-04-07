package me.whereareiam.yue.api;

/**
 * Interface for components that support reloading their configuration or state.
 *
 * <p>Classes implementing this interface can be reloaded during runtime,
 * allowing for dynamic updates to their configuration or state
 * without requiring a full restart of the application.</p>
 */
public interface Reloadable {
	/**
	 * Reloads the component's configuration and state.
	 * This method is called when the component's configuration
	 * need to be reinitialized during runtime.
	 */
	void reload();
}