package me.whereareiam.yui.api.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChannelDecoration {
	private String name;
	private String description;

	@Builder.Default
	private final boolean mention = false;
	private final String message;
}
