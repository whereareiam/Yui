package me.whereareiam.yue.api.model.plugin;

import lombok.Getter;

import java.util.List;

@Getter
public class Plugin {
	private String id;
	private String name;
	private String version;
	private List<String> authors;
	private String entrypoint;
}