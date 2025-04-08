package me.whereareiam.yue.adapter.module.resolver;

import me.whereareiam.yue.api.model.module.InternalModule;
import me.whereareiam.yue.shared.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ModuleVersionResolver implements ModuleResolver {
	private final Logger logger = LoggerFactory.getLogger(ModuleVersionResolver.class);

	@Override
	public boolean resolve(InternalModule module) {
		String currentVersion = Constants.VERSION;

		logger.debug("Checking version compatibility for module {}: current version: {}, required version: {}",
				module.getName(), currentVersion, module.getVersion()
		);

		for (String supportedVersion : module.getSupportedVersions()) {
			if (supportedVersion.equals(currentVersion)) {
				logger.debug("Module {} supports current version (exact match: {})",
						module.getName(), supportedVersion);
				return true;
			}

			try {
				if (currentVersion.matches(supportedVersion)) {
					logger.debug("Module {} supports current version (regex match: {})",
							module.getName(), supportedVersion);
					return true;
				}
			} catch (Exception e) {
				logger.warn("Failed to parse version regex for module {}: {}", module.getName(), e.getMessage());
			}
		}

		logger.warn("Module {} does not support current version: {}", module.getName(), currentVersion);
		return false;
	}
}