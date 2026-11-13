package me.whereareiam.yui.api.model.config.persistence;

import lombok.Getter;
import lombok.Setter;

/**
 * An H2 database in a file of Yui's data folder.
 *
 * <pre>{@code
 * type: H2
 * file: "database/yui"
 * }</pre>
 */
@Getter
@Setter
public final class EmbeddedPersistence extends Persistence {
	private String file;
}
