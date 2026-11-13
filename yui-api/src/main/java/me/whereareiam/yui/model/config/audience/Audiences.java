package me.whereareiam.yui.model.config.audience;

import me.whereareiam.conditia.model.Requirements;
import me.whereareiam.configura.annotation.PreserveUnknownFields;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code audiences.yml}: named lists of conditions that {@code requires:} reuses with {@code audience: <name>}.
 *
 * <pre>{@code
 * _version: 1
 * operator:
 *   - permission: MANAGE_SERVER
 * moderator:
 *   - any:
 *       - audience: operator
 *       - permission: MODERATE_MEMBERS
 * }</pre>
 */
@PreserveUnknownFields
public class Audiences {
	private final Map<String, Requirements> audiences;

	public Audiences() {
		this(Map.of());
	}

	/**
	 * @param audiences the lists by name
	 */
	public Audiences(@NotNull Map<String, Requirements> audiences) {
		this.audiences = Collections.unmodifiableMap(new LinkedHashMap<>(audiences));
	}

	/**
	 * @return every audience by name, in the file's order
	 */
	public @NotNull Map<String, Requirements> all() {
		return audiences;
	}
}
