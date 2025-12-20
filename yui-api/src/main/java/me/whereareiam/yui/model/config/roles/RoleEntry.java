package me.whereareiam.yui.model.config.roles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleEntry {
	private long id;
	private boolean sync;
	private String name;
	private String description;
}

