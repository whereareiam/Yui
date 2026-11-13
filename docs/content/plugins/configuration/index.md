---
title: Configuration
description: Read a plugin's own files from plugins/<id>/, with Yui's value types and reloads.
---

A plugin's files are in `plugins/<id>/`. Extend `PluginConfigProvider` for each: it reads the file the way Yui reads its own, durations, colours, locales, environment variables, enums in camel case, and reads it again on `/yui reload`. The file is never written; what it leaves out takes the defaults.

```java
@Getter
@Setter
public class GreetingSettings {
	private String emoji = "👋";
}
```

```java
@Component
public class SettingsProvider extends PluginConfigProvider<GreetingSettings> {
	@Override
	protected GreetingSettings load() {
		return read("settings", new GreetingSettings());
	}

	@Override
	public Class<GreetingSettings> getObjectType() {
		return GreetingSettings.class;
	}
}
```

The provider is a Spring factory: inject `ObjectProvider<GreetingSettings>` and call `getObject()` where the value is used, so a reload takes effect.

A key of two words is written in kebab case in the file and named with Jackson's `@JsonProperty("verified-role")` on the field.

## A file of named entries

For a file whose top level is a map of names the server owner chooses, such as Interests' `topics.yml`, read each entry with `readMap`:

```java
@Override
protected Topics load() {
	return new Topics(readMap("topics", Topic.class));
}
```

The entries keep the file's order. A value written in code, such as a default list of requirements, is read with `reader().parse("- audience: moderator", Requirements.class)`.
