package me.whereareiam.yue.api.event.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yue.api.event.Cancellable;

@Getter
@Setter
@RequiredArgsConstructor
public class RoleAddedEvent implements Cancellable {
	private final long User;
	private final long Role;
	private boolean cancelled;
}
