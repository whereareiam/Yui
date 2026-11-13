---
title: Audit trail
description: Post what happens in your server to channels, keep it for a while and search it with /yui audit.
---

The audit trail records what happens: members joining and leaving, roles given for facts, onboarding started and finished, updates found. Each entry has a name, such as `member.join`, and a category: `members`, `moderation` or `system`. `audit.yml` says where each category is posted:

```yaml
categories:
  members:
    channel: "123456789012345678"
  system:
    channel: "123456789012345679"
entries:
  member.leave: off
  onboarding.failed: system
store:
  enabled: true
  keep: 90d
```

- A category without a channel is not posted.
- `entries` sends one entry to another category's channel, or `off`.
- `store` keeps entries in the database for `keep`, also those without a channel, and `/yui audit [entry]` searches them, newest first.

An entry's text is in `messages/<locale>/audit.yml` (a plugin's in its own folder) and is written in `settings.yml`'s `locale`.
