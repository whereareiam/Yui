package me.whereareiam.yue.api.event;

public interface Cancellable {
	boolean isCancelled();

	void setCancelled(boolean cancelled);
}
