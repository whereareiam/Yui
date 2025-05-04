package me.whereareiam.yui.api.event.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.api.event.Cancellable;

@Getter
@Setter
@RequiredArgsConstructor
public class RoleAddedEvent implements Cancellable {
	private final long user;
	private final long role;
	private boolean cancelled;
}
