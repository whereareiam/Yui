package me.whereareiam.yui.fluctlight;

/**
 * Interface for defining typed extensions to Fluctlight.
 * <p>
 * Extensions allow modules to attach custom data to Fluctlight instances
 * without modifying the core Fluctlight class. Extension data is stored
 * in-memory only and is not persisted to the database.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyExtension implements FluctlightExtension<MyData> {
 *     public static final MyExtension INSTANCE = new MyExtension();
 *     
 *     @Override
 *     public String getNamespace() { return "mymodule"; }
 *     
 *     @Override
 *     public Class<MyData> getDataType() { return MyData.class; }
 *     
 *     @Override
 *     public MyData createDefault() { return new MyData(); }
 * }
 * 
 * // Usage
 * Fluctlight user = ...;
 * MyData data = user.getOrCreateExtension(MyExtension.INSTANCE);
 * }</pre>
 *
 * @param <T> The type of data this extension provides
 */
public interface FluctlightExtension<T> {
    /**
     * Returns the unique namespace for this extension.
     * <p>
     * The namespace is used to identify this extension's data in the Fluctlight
     * extensions map. It should be unique across all modules to avoid conflicts.
     * <p>
     * Convention: Use your module name as the namespace (e.g., "synapse", "economy").
     *
     * @return The namespace identifier
     */
    String getNamespace();
    
    /**
     * Returns the data type class for this extension.
     * <p>
     * This is used for type-safe casting when retrieving extension data.
     *
     * @return The data type class
     */
    Class<T> getDataType();
    
    /**
     * Creates a new default instance of the extension data.
     * <p>
     * This method is called when extension data is first accessed and doesn't exist yet.
     * It should return a new instance with sensible default values.
     *
     * @return A new default data instance
     */
    T createDefault();
}
