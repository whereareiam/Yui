package me.whereareiam.yui.common.config.template;

import me.whereareiam.yui.model.config.persistence.EmbeddedPersistence;
import me.whereareiam.yui.model.config.persistence.Persistence;
import me.whereareiam.yui.model.config.persistence.Pool;
import me.whereareiam.yui.model.config.persistence.ServerPersistence;
import me.whereareiam.yui.model.type.Duration;
import me.whereareiam.yui.type.DatabaseType;
import org.springframework.stereotype.Component;

/**
 * The values a {@code persistence.yml} setting takes when the file does not name it, for the database its
 * {@code type} chooses. Without a file, Yui keeps its data in H2.
 */
@Component
public class PersistenceTemplate {
	public Persistence supply(DatabaseType type) {
		Persistence persistence = switch (type) {
			case H2 -> embedded();
			case POSTGRESQL, MARIADB -> server();
		};
		persistence.setType(type);
		persistence.setPool(pool());

		return persistence;
	}

	private static EmbeddedPersistence embedded() {
		EmbeddedPersistence embedded = new EmbeddedPersistence();
		embedded.setFile("database/yui");

		return embedded;
	}

	private static ServerPersistence server() {
		ServerPersistence server = new ServerPersistence();
		server.setHost("127.0.0.1");
		server.setDatabase("yui");
		server.setUsername("yui");

		return server;
	}

	private static Pool pool() {
		Pool.Size size = new Pool.Size();
		size.setMin(2);
		size.setMax(10);

		Pool.Timeouts timeouts = new Pool.Timeouts();
		timeouts.setConnect(Duration.parse("30s"));
		timeouts.setIdle(Duration.parse("10m"));
		timeouts.setLifetime(Duration.parse("30m"));

		Pool pool = new Pool();
		pool.setSize(size);
		pool.setTimeouts(timeouts);

		return pool;
	}
}
