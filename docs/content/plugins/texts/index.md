---
title: Texts
description: Give a plugin's texts English defaults that server owners translate in its messages folder.
---

A plugin's texts are files of `plugins/<id>/messages/<locale>/`. Each file has English defaults in code, a `MessageDefaults` bean whose `file()` is the file's name; its keys start with the plugin's id and the file's name:

```java
@Getter
@Setter
public class CommandMessages {
	private Hello hello;

	@Getter
	@Setter
	public static class Hello {
		private String description;
		private String example;
		private String reply;
	}
}
```

```java
@Component
public class CommandMessagesDefaults implements MessageDefaults<CommandMessages> {
	@Override
	public @NotNull String file() {
		return "commands";
	}

	@Override
	public @NotNull CommandMessages defaults() {
		CommandMessages.Hello hello = new CommandMessages.Hello();
		hello.setDescription("Says hello");
		hello.setExample("/hello");
		hello.setReply("Hello {mention} {emoji}");

		CommandMessages messages = new CommandMessages();
		messages.setHello(hello);

		return messages;
	}
}
```

The reply's key is `greeting.commands.hello.reply`. Resolve a text in a member's language with `Translatable`:

```java
Translatable.text("greeting.commands.hello.reply").with("mention", member.getAsMention()).resolve(member);
```

A command's description, example and arguments are found by key: `<id>.commands.<command>.description`, `.example` and `.variables.<argument>`, with the command's id in camel case. Texts are ICU messages; values inserted are shown as written, never as markdown. A missing key resolves to the key itself.
