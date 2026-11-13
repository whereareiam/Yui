package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.yui.message.MessageDefaults;
import me.whereareiam.yui.model.config.messages.AuditMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditMessagesDefaults implements MessageDefaults<AuditMessages> {
	@Override
	public @NotNull String file() {
		return "audit";
	}

	@Override
	public @NotNull AuditMessages defaults() {
		return supply(new AuditMessages());
	}

	private AuditMessages supply(AuditMessages auditMessages) {
		AuditMessages.User user = new AuditMessages.User();
		
		AuditMessages.User.Join join = new AuditMessages.User.Join();
		join.setTitle("👋 User Joined");
		join.setDescription(List.of("{mention} joined the server"));
		AuditMessages.User.Join.Fields joinFields = new AuditMessages.User.Join.Fields();
		joinFields.setTarget("Target");
		joinFields.setAccountCreated("Account Created");
		join.setFields(joinFields);
		user.setJoin(join);

		AuditMessages.User.RoleChange role = new AuditMessages.User.RoleChange();
		role.setGiven("{mention} got {role} ({fact})");
		role.setTaken("{mention} lost {role} ({fact})");
		user.setRole(role);

		AuditMessages.User.Leave leave = new AuditMessages.User.Leave();
		leave.setTitle("👋 User Left");
		leave.setDescription(List.of("{mention} left the server"));
		AuditMessages.User.Leave.Fields leaveFields = new AuditMessages.User.Leave.Fields();
		leaveFields.setTarget("Target");
		leave.setFields(leaveFields);
		user.setLeave(leave);

		AuditMessages.User.Kick kick = new AuditMessages.User.Kick();
		kick.setTitle("🔨 User Kicked");
		kick.setDescription(List.of("{mention} was kicked from the server"));
		AuditMessages.User.Kick.Fields kickFields = new AuditMessages.User.Kick.Fields();
		kickFields.setTarget("Target");
		kickFields.setReason("Reason");
		kickFields.setModerator("Moderator");
		kick.setFields(kickFields);
		user.setKick(kick);

		auditMessages.setUser(user);

		AuditMessages.Update update = new AuditMessages.Update();

		AuditMessages.Update.Available available = new AuditMessages.Update.Available();
		AuditMessages.Update.Available.Release release = new AuditMessages.Update.Available.Release();
		release.setTitle("⬆️ Update available");
		release.setDescription(List.of("A new version of {name} is available!"));
		AuditMessages.Update.Available.Release.Fields releaseFields = new AuditMessages.Update.Available.Release.Fields();
		releaseFields.setCurrent("Current version");
		releaseFields.setLatest("Latest version");
		release.setFields(releaseFields);
		available.setRelease(release);

		AuditMessages.Update.Available.Dev dev = new AuditMessages.Update.Available.Dev();
		dev.setTitle("⬆️ Updates available");
		dev.setDescription(List.of("A newer dev build of {name} is available: {latest}"));
		AuditMessages.Update.Available.Dev.Fields devFields = new AuditMessages.Update.Available.Dev.Fields();
		devFields.setLatest("Latest version");
		devFields.setCurrent("Current version");
		dev.setFields(devFields);
		available.setDev(dev);

		update.setAvailable(available);

		AuditMessages.Update.LocalBuild localBuild = new AuditMessages.Update.LocalBuild();
		localBuild.setTitle("🛠️ Local development build");
		localBuild.setDescription(List.of("You are running a local dev build of {name}"));
		update.setLocalBuild(localBuild);

		auditMessages.setUpdate(update);

		return auditMessages;
	}
}
