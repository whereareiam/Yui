package me.whereareiam.yui.common.config.template.messages.command;

import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.yui.api.model.config.messages.command.AuditCommandMessages;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuditCommandMessagesTemplate implements DefaultsProvider<AuditCommandMessages> {
	@Override
	public AuditCommandMessages supply(AuditCommandMessages messages) {
		messages.setDescription("Search the kept audit trail");
		messages.setExample("/yui audit member.leave");
		messages.setVariables(Map.of("entry", "Only entries whose name contains this, such as member.leave"));
		messages.setTitle("Audit trail");
		messages.setEmpty("No kept entry matches.");
		messages.setDisabled("The audit trail is not kept; turn on store in audit.yml.");
		messages.setLine("**{posted}** · {category} · `{entry}`\n{content}");

		return messages;
	}
}
