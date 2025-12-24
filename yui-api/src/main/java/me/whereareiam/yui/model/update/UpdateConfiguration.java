package me.whereareiam.yui.model.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for update checking.
 * Specifies different sources for release and development builds.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConfiguration {
    /**
     * Update source for release builds.
     * Typically checks tagged releases from the provider.
     */
    private UpdateSource release;

    /**
     * Update source for development builds.
     * Typically checks commits or development versions.
     */
    private UpdateSource dev;
}
