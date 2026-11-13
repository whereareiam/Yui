package me.whereareiam.yui.model.config.persistence;

import lombok.Getter;
import lombok.Setter;

/**
 * A PostgreSQL or MariaDB server. Without a {@code port}, the type's own is used: 5432 for PostgreSQL, 3306 for
 * MariaDB.
 *
 * <pre>{@code
 * type: POSTGRESQL
 * host: "postgres.yui.svc"
 * database: "yui"
 * username: "yui"
 * password: "${DATABASE_PASSWORD}"
 * }</pre>
 */
@Getter
@Setter
public final class ServerPersistence extends Persistence {
	private String host;
	private Integer port;
	private String database;
	private String username;
	private String password;
}
