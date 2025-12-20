package me.whereareiam.yui.model.config.roles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncSettings {
	private int batchSize = 10;
	private long delayBetweenBatches = 500;
	private int retryAttempts = 3;
	private long retryDelay = 2000;
}
