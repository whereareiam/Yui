package me.whereareiam.yui.model.config.persistence;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.type.DatabaseType;

/**
 * {@code persistence.yml}: the database Yui stores its data in. Its {@code type} picks the shape, so a file holds
 * only the fields its database uses: {@link EmbeddedPersistence} for {@link DatabaseType#H2}, {@link ServerPersistence}
 * for a database server.
 */
@Getter
@Setter
public abstract sealed class Persistence permits EmbeddedPersistence, ServerPersistence {
	private DatabaseType type;
	private Pool pool;
}
