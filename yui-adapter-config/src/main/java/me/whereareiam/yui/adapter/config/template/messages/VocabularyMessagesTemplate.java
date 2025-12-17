package me.whereareiam.yui.adapter.config.template.messages;

import me.whereareiam.yui.model.config.messages.VocabularyMessages;
import me.whereareiam.yui.config.DefaultConfig;
import org.springframework.stereotype.Component;

@Component
public class VocabularyMessagesTemplate implements DefaultConfig<VocabularyMessages> {
	@Override
	public VocabularyMessages getDefault() {
		VocabularyMessages vocabulary = new VocabularyMessages();
		vocabulary.setCancel("Cancel");
		vocabulary.setProceed("Continue");
		vocabulary.setConfirm("Confirm");
		vocabulary.setBack("Back");
		vocabulary.setNext("Next");
		vocabulary.setYes("Yes");
		vocabulary.setNo("No");

		VocabularyMessages.Category category = new VocabularyMessages.Category();
		category.setUtility("🛠️ Utility");
		category.setFun("🎮 Fun");
		category.setModeration("🛡️ Moderation");
		category.setAdministration("⚙️ Administration");
		category.setNone("📌 None");
		vocabulary.setCategory(category);

		return vocabulary;
	}
}


