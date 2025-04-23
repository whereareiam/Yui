package me.whereareiam.yue.api.event.role;

import me.whereareiam.yue.api.event.YueEvent;

public class RoleRemovedEvent extends YueEvent {
	private final long User;
	private final long Role;

	public RoleRemovedEvent(Object source, long user, long role) {
		super(source);

		User = user;
		Role = role;
	}

	public long getUser() {
		return User;
	}

	public long getRole() {
		return Role;
	}
}
