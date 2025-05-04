package me.whereareiam.yui.api.model.config.messages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Messages {
	private GeneralMessages general;
	private CommandMessages commands;
	private VocabularyMessages vocabulary;
}
