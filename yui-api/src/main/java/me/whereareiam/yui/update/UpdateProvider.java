package me.whereareiam.yui.update;

import me.whereareiam.yui.model.update.UpdateSource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Provides update information from various sources.
 * Implementations can check for updates from GitHub, custom CDNs, or any other source.
 */
public interface UpdateProvider {
    /**
     * Gets the unique identifier for this provider.
     *
     * @return the provider ID (e.g., "github", "custom-cdn")
     */
    String getId();

    /**
     * Fetches the latest version from the update source.
     *
     * @param source the update source configuration
     * @return the latest version string, or empty if not available
     * @throws IOException if an error occurs while fetching the update
     */
    Optional<String> fetchLatest(UpdateSource source) throws IOException;

    /**
     * Fetches recent updates or commits from the update source.
     * Primarily used for development builds to check commit count.
     *
     * @param source the update source configuration
     * @param limit  the maximum number of recent updates to fetch
     * @return list of recent version identifiers (tags, commit hashes, etc.)
     * @throws IOException if an error occurs while fetching updates
     */
    List<String> fetchRecentUpdates(UpdateSource source, int limit) throws IOException;
}
