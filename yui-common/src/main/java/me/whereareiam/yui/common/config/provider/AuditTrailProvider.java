package me.whereareiam.yui.common.config.provider;

import me.whereareiam.yui.api.model.config.audit.AuditTrail;
import org.springframework.stereotype.Component;

/**
 * Reads {@code audit.yml}. Without a file no category has a channel, and nothing is posted.
 */
@Component
public class AuditTrailProvider extends DefaultConfigProvider<AuditTrail> {
	@Override
	protected AuditTrail load() {
		return read("audit", new AuditTrail());
	}

	@Override
	public Class<AuditTrail> getObjectType() {
		return AuditTrail.class;
	}
}
