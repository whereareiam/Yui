package me.whereareiam.yue.api.output.service;

public interface RoleService {
	void addRole(long id);

	void removeRole(long id);

	boolean roleExists(long id);

	long[] getAvailableRoles();
}