package me.whereareiam.yui.api.model.config.settings.database;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseSettings {
	private String hostname;
	private int port;
	private String database;
	private String username;
	private String password;
	private HikariSettings hikari;
}
