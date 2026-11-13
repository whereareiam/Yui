package me.whereareiam.yui.common.config.provider;

import me.whereareiam.yui.api.model.config.audience.Audiences;
import me.whereareiam.yui.common.config.template.AudiencesTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reads {@code audiences.yml}; Yui's own audiences, {@code operator} and {@code moderator}, are there unless the file
 * changes them.
 */
@Component
public class AudiencesProvider extends DefaultConfigProvider<Audiences> {
	@Autowired
	private AudiencesTemplate template;

	@Override
	protected Audiences load() {
		return read("audiences", template.supply(new Audiences()));
	}

	@Override
	public Class<Audiences> getObjectType() {
		return Audiences.class;
	}
}
