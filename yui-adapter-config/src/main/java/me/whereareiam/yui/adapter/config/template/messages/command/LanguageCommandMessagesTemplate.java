package me.whereareiam.yui.adapter.config.template.messages.command;

import me.whereareiam.yui.api.model.config.messages.command.LanguageCommandMessages;
import me.whereareiam.yui.api.output.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LanguageCommandMessagesTemplate implements DefaultConfig<LanguageCommandMessages> {
	@Override
	public LanguageCommandMessages getDefault() {
		LanguageCommandMessages language = new LanguageCommandMessages();
		language.setDescription("Change your language preferences");
		language.setExample("/language");

		LanguageCommandMessages.Primary primary = new LanguageCommandMessages.Primary();
		primary.setTitle("Select Primary Language");
		primary.setDescription(List.of("Please select your primary language by clicking one of the buttons below."));
		language.setPrimary(primary);

		LanguageCommandMessages.Additional additional = new LanguageCommandMessages.Additional();
		additional.setTitle("Select Additional Languages & Channels");
		additional.setDescription(List.of(
				"Choose any additional languages you understand. These will be used for translations if your primary language is not available.",
				"",
				"For each selected language, a dedicated chat channel will be added, allowing you to connect with others in that language.",
				"Your primary language will also have its own channel automatically."
		));
		language.setAdditional(additional);

		LanguageCommandMessages.Success success = new LanguageCommandMessages.Success();
		success.setTitle("Language Settings Updated");
		language.setSuccess(success);

		LanguageCommandMessages.Cancelled cancelled = new LanguageCommandMessages.Cancelled();
		cancelled.setTitle("Language Selection Cancelled");
		language.setCancelled(cancelled);

		return language;
	}
}
