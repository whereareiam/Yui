package me.whereareiam.yue.api.model.config.settings.database;

public class DatabaseSettings {
	private String hostname;
	private int port;
	private String database;
	private String username;
	private String password;
	private HikariSettings hikari;

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public HikariSettings getHikari() {
		return hikari;
	}

	public void setHikari(HikariSettings hikari) {
		this.hikari = hikari;
	}
}
