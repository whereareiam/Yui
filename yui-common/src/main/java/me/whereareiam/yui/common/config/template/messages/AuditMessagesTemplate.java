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

		return auditMessages;
	}
}
