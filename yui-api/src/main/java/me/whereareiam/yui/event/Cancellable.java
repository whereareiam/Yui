package me.whereareiam.yui.event;

public interface Cancellable {
	boolean isCancelled();

	void setCancelled(boolean cancelled);
}
