package me.whereareiam.yui.persistence;

public interface RolePersistence {
	void addRole(long id);

	void removeRole(long id);

	boolean roleExists(long id);

	long[] getAvailableRoles();
}