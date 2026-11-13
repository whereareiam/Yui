---
title: Conversations
description: How Yui talks to members in private, in direct messages, private threads or temporary channels.
---

A conversation is Yui talking with one or more members for one purpose, such as onboarding, out of everyone else's sight. It happens in one of three types, tried in the order `via` says until one works:

| Type | Where | Needs |
| --- | --- | --- |
| `direct` | a direct message | members who accept messages from server members |
| `thread` | a private thread | a `parent` channel, the bot allowed to create private threads there |
| `channel` | a temporary channel | `categories`, the bot allowed to manage channels there |

```yaml
types:
  thread:
    parent: "123456789012345678"
  channel:
    categories: ["123456789012345679"]
defaults:
  via: [direct, thread, channel]
  mention: true
  staff: ["123456789012345680"]
  close:
    idle: 15m
    after: 2h
    warn: 30s
  limit:
    open: 1
    cooldown: 10m
  transcript:
    audit: true
    member: false
```

- `types.<type>.enabled: false` never uses a type. A thread needs `parent`, a channel at least one category, before it can open; `thread.archive` (`1h`) is how long a thread stays without messages before Discord archives it, and `channel.purge` deletes temporary channels Yui finds at startup.
- `staff` are roles that see every conversation and can help in it.
- `close` closes a conversation once it was `idle` for that long or open `after` that long, posting a warning `warn` before.
- `limit` keeps a member to `open` conversations of a purpose at once, and waits `cooldown` before they get the next.
- `transcript` sends the conversation's messages to the audit trail, or to the member, when it closes.

A plugin may change any of this for its own purpose: onboarding, for one, keeps its conversation open until the member is done, however long that takes.
