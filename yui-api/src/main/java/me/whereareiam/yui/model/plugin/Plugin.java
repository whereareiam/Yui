package me.whereareiam.yui.model.plugin;

import lombok.Getter;
import me.whereareiam.yui.model.update.UpdateConfiguration;

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
	private UpdateConfiguration updater;
}