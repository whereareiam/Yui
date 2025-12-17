package me.whereareiam.yui.event.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.whereareiam.yui.model.profile.UserProfile;

@Getter
@Setter
@RequiredArgsConstructor
public class UserProfileCreatedEvent {
	private final UserProfile userProfile;
}
