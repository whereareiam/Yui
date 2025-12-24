package me.whereareiam.yui.model.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a source for update information.
 * Contains the provider identifier and the project/resource ID.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSource {
    /**
     * The provider ID (e.g., "github", "custom-cdn").
     * Must match a registered UpdateProvider's getId() value.
     */
    private String provider;

    /**
     * The project or resource identifier specific to the provider.
     * For GitHub: "owner/repository"
     * For custom providers: any identifier the provider understands
     */
    private String id;
}
