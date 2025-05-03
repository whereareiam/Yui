package me.whereareiam.yue.api.event.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yue.api.event.Cancellable;

@Getter
@Setter
@RequiredArgsConstructor
public class RoleRemovedEvent implements Cancellable {
	private final long user;
	private final long role;
	private boolean cancelled;
}
