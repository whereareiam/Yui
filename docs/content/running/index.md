---
title: Getting started
description: Start Yui for your Discord server with the database it brings along.
---

Yui is a Discord bot for one server. It knows its members and the languages they speak, opens private conversations with them, keeps an audit trail of what happens and gives roles that follow facts about members, such as a language they speak or a product they use. Plugins add the rest: Yui's own **Onboarding** greets new members, **Interests** lets them pick topics.

## What you need

- Java 25.
- A Discord application with a bot, its token, and the bot invited to your server. In the Developer Portal, switch on the **Server Members** and **Message Content** intents: Yui asks for both.
- The id of your server (in Discord, right-click the server with developer mode on, then *Copy Server ID*).

## First start

Put `Yui.jar` into an empty folder, the folder Yui keeps its files in, and write `settings.yml` beside it:

```yaml
token: "${DISCORD_TOKEN}"
guild: "123456789012345678"
locale: "en-US"
```

Start it with the token in the environment:

```bash
DISCORD_TOKEN=... java -jar Yui.jar
```

On the first start Yui downloads its libraries into `.libraries/`, creates an H2 database in `database/` and connects to Discord. Once the bot shows as online in your server, `/yui status` answers. Any file Yui reads but you did not write takes its defaults: Yui never writes its configuration files, so the folder holds only what you decided.

To keep the files elsewhere than the folder Yui starts in, name it with `-Dapp.dir=/path/to/yui`.

## Next

- [Configuration](./configuration/index.md): every file, and what a missing one means.
- [Permissions](./permissions/index.md): who may use which command.
- [Plugins](./plugins/index.md): adding Onboarding and other plugins.

## When it does not start

| What you see | Check |
| --- | --- |
| `Disallowed intents` in the log | Switch on **Server Members** and **Message Content** for the bot in the Developer Portal, or remove them from `intents:`. |
| The bot is online but answers nothing | `guild` is the id of the server the bot is in, and the bot was invited with the `applications.commands` scope. |
| `Not a version` for a plugin | The plugin was built without a version; rebuild it with Yui's Gradle plugin (see [Writing plugins](./plugins/index.md)). |
