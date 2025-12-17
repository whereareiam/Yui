package me.whereareiam.yui.model.plugin;

import lombok.Getter;

import java.util.List;

@Getter
@SuppressWarnings("unused")
public class Plugin {
	private String id;
	private String name;
	private String version;
	private List<Dependency> dependencies;
	private List<String> authors;
	private String entrypoint;
}