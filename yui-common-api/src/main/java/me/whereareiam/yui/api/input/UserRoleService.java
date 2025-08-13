package me.whereareiam.yui.api.input;

public interface UserRoleService {
	void addRoleToUser(long userId, long roleId);

	void removeRoleFromUser(long userId, long roleId);

	void syncUser(long userId);

	void syncAll();

	/**
	 * Check if a user is currently being synced by the bot
	 */
	boolean isUserBeingSynced(long userId);
}
