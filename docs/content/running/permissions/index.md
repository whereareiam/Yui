---
title: Permissions
description: Decide who may use a command with requires lists, conditions and named audiences.
---

Who may use a command is a `requires:` list in `commands.yml`. Every condition of the list must hold; the first one that does not is what the member is told, through the command's `denied` text.

```yaml
commands:
  clear:
    requires:
      - audience: moderator
      - scope: guild
      - channel-type: [text, thread]
```

A command without `requires` is open to everyone. A command whose list comes down to Discord permissions alone (`permission: MANAGE_SERVER`, or an audience that is one) is also registered with that permission in Discord, so members who lack it do not see the command at all.

## Conditions

| Condition | Holds when |
| --- | --- |
| `permission: MODERATE_MEMBERS` | the member has every listed Discord permission in the server |
| `role: "123…"` | the member holds every listed role |
| `user: "123…"` | the member is one of the listed users |
| `language: [de, fr]` | the member speaks one of the languages, as primary or additional |
| `joined: 7d` | the member joined the server at least that long ago |
| `account: 30d` | the member's Discord account is at least that old |
| `scope: guild` | the command runs in the server; `direct` in a direct message |
| `channels: "123…"` | the command runs in one of the listed channels |
| `channel-type: [text, thread]` | the command runs in a channel of one of the kinds |
| `any: [ … ]` | at least one of the nested conditions holds |
| `not: { … }` | the nested condition does not hold |
| `audience: moderator` | the named list holds, see below |

Plugins add conditions of their own; their names carry no prefix (`verified: true` from Onboarding, `owns: [yui]` from Arcadeya's products), only their audiences do.

## Audiences

An audience is a named list of conditions in `audiences.yml`, used with `audience: <name>`. Yui brings two:

```yaml
operator:
  - permission: MANAGE_SERVER
moderator:
  - any:
      - audience: operator
      - permission: MODERATE_MEMBERS
```

Change one by writing it in your `audiences.yml`; add your own the same way:

```yaml
staff:
  - role: "123456789012345678"
```

A plugin's audiences are written in its own folder, `plugins/<id>/audiences.yml`, with bare names, and used with the plugin's id: Onboarding's `verified` is `audience: onboarding:verified`. A plugin's audience you write in its file replaces the plugin's own.

An audience may refer to another. A loop or an unknown name stops Yui at startup and names the file and the path. When a plugin stops, a list that refers to its conditions or audiences denies everyone until it starts again, and the log says which.
