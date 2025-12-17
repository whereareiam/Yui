package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.configura.TemplateProvider;
import me.whereareiam.yui.model.config.messages.VocabularyMessages;
import org.springframework.stereotype.Component;

@Component
public class VocabularyMessagesTemplate implements TemplateProvider<VocabularyMessages> {
	@Override
	public VocabularyMessages supply(VocabularyMessages vocabulary) {
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


