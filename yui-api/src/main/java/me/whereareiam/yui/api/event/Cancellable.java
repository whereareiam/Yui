package me.whereareiam.yui.api.event;

public interface Cancellable {
	boolean isCancelled();

	void setCancelled(boolean cancelled);
}
