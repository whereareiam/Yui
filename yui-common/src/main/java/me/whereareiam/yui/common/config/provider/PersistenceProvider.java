package me.whereareiam.yui.common.config.provider;

import com.fasterxml.jackson.databind.JsonNode;
import me.whereareiam.yui.common.config.template.PersistenceTemplate;
import me.whereareiam.yui.model.config.persistence.Persistence;
import me.whereareiam.yui.type.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reads {@code persistence.yml}. Its {@code type} picks which shape the file is bound to, so the defaults are those
 * of the database it names; without a file, Yui keeps its data in H2.
 */
@Component
public class PersistenceProvider extends DefaultConfigProvider<Persistence> {
	@Autowired
	private PersistenceTemplate template;

	@Override
	protected Persistence load() {
		return read("persistence", template.supply(type()));
	}

	private DatabaseType type() {
		JsonNode type = readNode("persistence").path("type");
		if (type.isMissingNode()) return DatabaseType.H2;

		try {
			return DatabaseType.valueOf(type.asText());
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("persistence.yml: type is H2, POSTGRESQL or MARIADB, not " + type.asText(), exception);
		}
	}

	@Override
	public Class<Persistence> getObjectType() {
		return Persistence.class;
	}
}
