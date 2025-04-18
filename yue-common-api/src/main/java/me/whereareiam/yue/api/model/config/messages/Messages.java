package me.whereareiam.yue.api.model.config.messages;

public class Messages {
	private CommandMessages commands;
	private VocabularyMessages vocabulary;

	public CommandMessages getCommands() {
		return commands;
	}

	public void setCommands(CommandMessages commands) {
		this.commands = commands;
	}

	public VocabularyMessages getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(VocabularyMessages vocabulary) {
		this.vocabulary = vocabulary;
	}
}
