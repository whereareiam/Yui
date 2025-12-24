package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.messages.AuditMessages;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditMessagesTemplate implements TemplateProvider<AuditMessages> {
	@Override
	public AuditMessages supply(AuditMessages auditMessages) {
		AuditMessages.User user = new AuditMessages.User();
		
		AuditMessages.User.Join join = new AuditMessages.User.Join();
		join.setTitle("👋 User Joined");
		join.setDescription(List.of("<p:mention> joined the server"));
		AuditMessages.User.Join.Fields joinFields = new AuditMessages.User.Join.Fields();
		joinFields.setTarget("Target");
		joinFields.setAccountCreated("Account Created");
		join.setFields(joinFields);
		user.setJoin(join);

		AuditMessages.User.Leave leave = new AuditMessages.User.Leave();
		leave.setTitle("👋 User Left");
		leave.setDescription(List.of("<p:mention> left the server"));
		AuditMessages.User.Leave.Fields leaveFields = new AuditMessages.User.Leave.Fields();
		leaveFields.setTarget("Target");
		leave.setFields(leaveFields);
		user.setLeave(leave);

		AuditMessages.User.Kick kick = new AuditMessages.User.Kick();
		kick.setTitle("🔨 User Kicked");
		kick.setDescription(List.of("<p:mention> was kicked from the server"));
		AuditMessages.User.Kick.Fields kickFields = new AuditMessages.User.Kick.Fields();
		kickFields.setTarget("Target");
		kickFields.setReason("Reason");
		kickFields.setModerator("Moderator");
		kick.setFields(kickFields);
		user.setKick(kick);

		auditMessages.setUser(user);

		AuditMessages.Update update = new AuditMessages.Update();

		AuditMessages.Update.Available available = new AuditMessages.Update.Available();
		available.setTitle("⬆️ Update available");
		available.setDescription(List.of("A new version of <p:name> is available!"));
		AuditMessages.Update.Available.Fields availableFields = new AuditMessages.Update.Available.Fields();
		availableFields.setCurrent("Current version");
		availableFields.setLatest("Latest version");
		available.setFields(availableFields);
		update.setAvailable(available);

		AuditMessages.Update.Behind behind = new AuditMessages.Update.Behind();
		behind.setTitle("⬆️ Updates available");
		behind.setDescription(List.of("You are <p:commits> commit<if commits!=1>s</if> behind the latest dev build of <p:name>"));
		AuditMessages.Update.Behind.Fields behindFields = new AuditMessages.Update.Behind.Fields();
		behindFields.setCommits("Commits behind");
		behindFields.setCurrent("Current version");
		behind.setFields(behindFields);
		update.setBehind(behind);

		AuditMessages.Update.LocalBuild localBuild = new AuditMessages.Update.LocalBuild();
		localBuild.setTitle("🛠️ Local development build");
		localBuild.setDescription(List.of("You are running a local dev build of <p:name>"));
		update.setLocalBuild(localBuild);

		auditMessages.setUpdate(update);

		return auditMessages;
	}
}
