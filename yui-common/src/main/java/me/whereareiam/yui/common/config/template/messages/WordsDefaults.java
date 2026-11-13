package me.whereareiam.yui.common.config.template.messages;

import me.whereareiam.yui.message.MessageDefaults;
import me.whereareiam.yui.model.config.messages.Words;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class WordsDefaults implements MessageDefaults<Words> {
	@Override
	public @NotNull String file() {
		return "words";
	}

	@Override
	public @NotNull Words defaults() {
		Words.Category category = new Words.Category();
		category.setUtility("🛠️ Utility");
		category.setFun("🎮 Fun");
		category.setModeration("🛡️ Moderation");
		category.setAdministration("⚙️ Administration");
		category.setNone("📌 None");

		Words words = new Words();
		words.setCancel("Cancel");
		words.setProceed("Continue");
		words.setConfirm("Confirm");
		words.setEdit("Edit");
		words.setBack("Back");
		words.setNext("Next");
		words.setYes("Yes");
		words.setNo("No");
		words.setCategory(category);

		return words;
	}
}
