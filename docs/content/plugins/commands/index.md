---
title: Commands and buttons
description: Define commands the server owner can configure, and handle buttons and menus under the plugin's id.
---

A command is a `CommandDefinition` from the plugin's `DefinitionProvider` (see [Your first plugin](../index.md#a-command)) and a method with `@Definition` and `@Command`:

```java
@Definition("verify")
@Command("verify <user>")
public void onCommand(Interaction interaction, @Argument("user") Fluctlight target) {
	interaction.deferReply(true).queue();
	// ...
}
```

The definition's `requires` is the default the server owner may change in the plugin's `commands.yml`, read with `reader().parse(...)`. Yui checks it before the method runs and answers with the command's `denied` text when it fails; a command whose requirements come down to Discord permissions is also hidden from members without them.

## Buttons and menus

Inject `InteractionService`: the plugin's own one qualifies every path with the plugin's id, so two plugins never handle each other's clicks.

```java
Button done = interactions.createButton(ButtonStyle.SUCCESS, "done", label, attributes);
```

```java
@ComponentListener("done")
public void onDone(ButtonInteractionEvent event) {
	event.deferEdit().queue();
	String session = interactions.getAttributes(event).get(JourneyKeys.SESSION_ID).orElse(null);
	// ...
}
```

`ComponentAttributes` carry values with a button or menu to its handler; a payload carries one string. Use the injected service rather than the static `Components`, which answers for Yui's own paths.
