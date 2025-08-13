package me.whereareiam.yui.api.input;

import java.util.Collection;

/**
 * A generic registry interface that provides registration functionality for
 * different types of components in the framework.
 *
 * <p>This interface allows for type-safe registration of various elements
 * such as commands, events, or integrations. The generic type parameter T
 * represents the type of element that can be registered.</p>
 *
 * @param <T> the type of element that can be registered in this registry
 */
public interface Registry<T> {
	/**
	 * Registers a new element in this registry.
	 *
	 * @param object the element to register
	 */
	void register(T object);

	/**
	 * Gets all registered elements from this registry.
	 *
	 * @return a collection of all registered elements
	 */
	Collection<T> getAll();
}