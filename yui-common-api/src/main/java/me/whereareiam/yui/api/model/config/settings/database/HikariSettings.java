package me.whereareiam.yui.api.model.config.settings.database;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HikariSettings {
	private int minimumIdle;
	private int maximumPoolSize;
	private long connectionTimeout;
	private long idleTimeout;
	private long maxLifetime;
}
