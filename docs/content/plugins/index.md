---
title: Your first plugin
description: Build a plugin with a command and its texts, and load it into Yui.
---

A plugin is a jar that Yui loads from its `plugins/` folder into a Spring context of its own, beneath Yui's. Yui registers what the context holds by itself, its commands, texts, listeners, conditions, journey steps and tables, and removes all of it when the plugin stops. The example below is `examples/plugin` in Yui's repository: `/hello` greets whoever runs it.

## The build

Yui's Gradle plugin writes the plugin's descriptor, `yui-plugin.yml`, from the build and packs the jar:

```kotlin
plugins {
    id("me.whereareiam.yui.plugin") version "<yui version>"
    id("io.freefair.lombok") version "9.8.3"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

yuiPlugin {
    id.set("greeting")
    main.set("com.example.greeting.Greeting")
}

dependencies {
    compileOnly("me.whereareiam.yui:api:<yui version>")
}
```

| `yuiPlugin` | Meaning |
| --- | --- |
| `id` | the plugin's id; its folder is `plugins/<id>/` and its texts' keys start with it |
| `main` | the main class |
| `dependencies` | ids of plugins it needs; they start first |
| `exports` | packages plugins that depend on it may use, such as its own `-api` |
| `updates` | where its releases are, `GITHUB:owner/repository`, for Yui's updater |

Modules listed under `bundled` in `dependencies { }` are packed into the jar; a plugin's own `-api` goes there. Yui's API, JDA, Spring, Conditia, Semantica and Jakarta Persistence are provided by Yui and are never packed.

## The main class

```java
@Plugin
public class Greeting implements YuiPlugin {
	public static final String ID = "greeting";
}
```

`@Plugin` makes the class the configuration of the plugin's context and scans its package. `YuiPlugin` has `enable` and `disable` for what the plugin does beyond what Yui registers.

## A command

A command has a definition, which the server owner may change in `plugins/greeting/commands.yml`, and a method:

```java
@Component
public class CommandsProvider extends PluginConfigProvider<Commands> implements DefinitionProvider {
	@Override
	protected Commands load() {
		Commands defaults = new Commands();
		defaults.getCommands().put("hello", new CommandDefinition(
				true, List.of("hello"), "{alias}", CommandCategory.FUN,
				new CommandCooldown(true, Duration.ofSeconds(5), null), Requirements.none(), false));

		return read("commands", defaults);
	}

	@Override
	public Class<Commands> getObjectType() {
		return Commands.class;
	}

	@Override
	public @NotNull String id() {
		return Greeting.ID;
	}

	@Override
	public @NotNull Source source() {
		return Source.EXTERNAL;
	}

	@Override
	public @NotNull Map<String, CommandDefinition> definitions() {
		return get().getCommands();
	}
}
```

```java
@Component
@RequiredArgsConstructor
public class HelloCommand {
	private final ObjectProvider<GreetingSettings> settings;

	@Definition("hello")
	@Command("hello")
	public void onCommand(Interaction interaction) {
		Fluctlight member = interaction.fluctlight();
		String reply = Translatable.text("greeting.commands.hello.reply")
				.with("mention", member.getAsMention())
				.with("emoji", settings.getObject().getEmoji())
				.resolve(member);

		interaction.replyCallback().reply(reply).setEphemeral(true).queue();
	}
}
```

The usage `{alias}` makes `/hello` a command of its own; `{command} {alias}` makes it `/yui hello`. Its description and reply are [texts](./texts/index.md).

## Loading it

```bash
./gradlew build
cp build/libs/greeting-1.0.0.jar /path/to/yui/plugins/
```

Then `/yui plugin` lists `greeting` as enabled, and `/hello` answers. A plugin that fails to start is listed as failed, with the reason in Yui's log.

Next: [Configuration](./configuration/index.md) and [Texts](./texts/index.md).
