package me.whereareiam.yui.model.config.settings.database;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.yui.type.DatabaseType;

/**
 * Where Yui stores its data.
 *
 * <p>{@link DatabaseType#H2} uses {@link #getFile() file} only; a server database uses the host, port, database name
 * and credentials.</p>
 */
@Getter
@Setter
public class DatabaseSettings {
	private DatabaseType type;
	private String file;
	private String hostname;
	private int port;
	private String database;
	private String username;
	private String password;
	private HikariSettings hikari;
}
