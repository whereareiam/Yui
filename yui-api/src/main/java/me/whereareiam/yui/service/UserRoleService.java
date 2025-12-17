package me.whereareiam.yui.service;

public interface UserRoleService {
	void addRoleToUser(long userId, long roleId);

	void removeRoleFromUser(long userId, long roleId);

	void syncUser(long userId);

	void syncAll();
}
