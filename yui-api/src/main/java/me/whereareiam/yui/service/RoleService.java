package me.whereareiam.yui.service;

public interface RoleService {
	void addRole(long id);

	void removeRole(long id);

	boolean roleExists(long id);

	long[] getAvailableRoles();
}