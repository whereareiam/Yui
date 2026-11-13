package me.whereareiam.yui.api.type;

/**
 * The database Yui stores its data in, chosen in the settings.
 */
public enum DatabaseType {
	/**
	 * An H2 database in a file in Yui's data folder. Needs no server.
	 */
	H2,

	/**
	 * A PostgreSQL server.
	 */
	POSTGRESQL,

	/**
	 * A MariaDB server.
	 */
	MARIADB
}
