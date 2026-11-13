package me.whereareiam.yui.api.permission;

import me.whereareiam.yui.api.model.config.audience.Audiences;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin's own audiences, as {@code plugins/<id>/audiences.yml} has them unless the file changes them. Names are
 * bare; they are used with the plugin's id ({@code audience: verification:verified}).
 *
 * <pre>{@code
 * @Component
 * public class VerificationAudiences implements AudienceDefaults {
 *     public Audiences defaults() {
 *         return new Audiences(Map.of("verified", reader.parse("- verified: true", Requirements.class)));
 *     }
 * }
 * }</pre>
 */
public interface AudienceDefaults {
	/**
	 * @return the audiences by bare name
	 */
	@NotNull Audiences defaults();
}
