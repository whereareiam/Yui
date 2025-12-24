package me.whereareiam.yui.model.config.roles;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Roles {
	private List<RoleEntry> roles;
	private SyncSettings settings;
}