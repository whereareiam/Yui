---
title: Configuration
description: The files in Yui's folder, what each decides and what a missing one means.
---

Yui reads its configuration from YAML files in its folder and never writes them. A file you do not write takes its defaults, and so does a setting a file leaves out. Values may name environment variables, `"${DATABASE_PASSWORD}"`, so secrets stay out of the files. `/yui reload` reads the files again without a restart.

```text
settings.yml          the bot's token, your server, the default language, the updater
persistence.yml       the database
languages.yml         the languages members may say they speak
audiences.yml         named lists of conditions, see Permissions
audit.yml             where the audit trail is posted and how long it is kept
commands.yml          which commands are on and who may use them
conversations.yml     how Yui talks to members in private
style/
  palette.yml         named colours
  embeds.yml          how each kind of embed looks
messages/<locale>/    texts, see Texts
plugins/<id>/         each plugin's own files, the same shape
```

Durations are written `30s`, `15m`, `2h`, `1d` or combined, `1h30m`. Ids of roles, channels and users are written as strings in quotes.

## settings.yml

```yaml
token: "${DISCORD_TOKEN}"
guild: "123456789012345678"
locale: "en-US"
intents: [GUILD_MESSAGES, MESSAGE_CONTENT, GUILD_MEMBERS, GUILD_PRESENCES, GUILD_VOICE_STATES, GUILD_EXPRESSIONS, SCHEDULED_EVENTS]
updater:
  interval: 1d
  check: [yui, plugins]
  warn: [updates, localBuilds]
```

| Setting | Default | Meaning |
| --- | --- | --- |
| `token` | none | The bot's token. |
| `guild` | none | The id of the one server Yui serves. |
| `locale` | `en-US` | The language of members who have not said theirs, and of the audit trail. |
| `intents` | as above | The gateway intents Yui asks Discord for. |
| `updater.interval` | `1d` | How often Yui looks for new versions. |
| `updater.check` | `yui`, `plugins` | What it looks for. |
| `updater.warn` | `updates`, `localBuilds` | What it posts to the audit trail's system category. |

## persistence.yml

Without the file, Yui keeps its data in an H2 database at `database/yui`. For a database server:

```yaml
type: POSTGRESQL
host: "postgres.example.internal"
database: "yui"
username: "yui"
password: "${DATABASE_PASSWORD}"
```

`type` is `H2`, `POSTGRESQL` or `MARIADB`. A server's `port` defaults to 5432 for PostgreSQL and 3306 for MariaDB. `pool` sets the connections: `size: { min: 2, max: 10 }` and `timeouts: { connect: 30s, idle: 10m, lifetime: 30m }` are the defaults. Yui creates and updates its tables itself, and each plugin with tables does the same for its own.

## languages.yml

The languages members may say they speak, by Discord locale. A listed language is offered; its role, if any, is held by every member who speaks it.

```yaml
en-US:
  emoji: "🇺🇸"
  role: "123456789012345678"
de:
  emoji: "🇩🇪"
```

A language's name is a text of `messages/<locale>/languages.yml`; without one, Discord's own name for it is shown. Whether Yui has texts in a language is a separate matter: members may speak a language Yui does not translate to.

## style/

`palette.yml` names colours; `embeds.yml` gives each kind of embed (`primary`, `secondary`, `success`, `warning`, `error`, `info`) a colour, by palette name or as a hex value, and optionally a footer, an author, a thumbnail or a timestamp:

```yaml
# style/palette.yml
brand: "#B57E3F"
```

```yaml
# style/embeds.yml
primary:
  color: brand
  timestamp: true
error:
  color: "#E03131"
```

The texts of an embed's author and footer are in `messages/<locale>/embeds.yml`.

## commands.yml

Each command can be switched off, renamed, limited to some members and given a cooldown. What a file names replaces the default; the rest stays.

```yaml
commands:
  clear:
    enabled: true
    aliases: [clear, purge]
    requires:
      - audience: moderator
    cooldown: { enabled: true, duration: 10s }
    hide: false
```

`requires` is described in [Permissions](../permissions/index.md); `hide` leaves a command out of `/help`. A cooldown with a `group` is shared by every command of that group. Descriptions and replies are texts, in `messages/<locale>/commands.yml`.

The other files have pages of their own: [audiences.yml](../permissions/index.md), [audit.yml](../audit/index.md), [conversations.yml](../conversations/index.md).
