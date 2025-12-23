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
	
	// Debouncing - wait time after last change before syncing to Discord
	private long debounceMillis = 500;
	
	// Persistence batching - batch multiple DB writes together
	private int persistenceBatchSize = 50;
	private long persistenceFlushMillis = 1000;
}
