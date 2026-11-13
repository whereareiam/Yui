---
title: Onboarding
description: Greet new members in a private conversation, ask their languages and verify them.
---

Onboarding greets each new member in a [conversation](../conversations/index.md) of its own. They pick the language they speak best, then any others, then read a closing text, and are verified: they get the verified role, and the condition `verified: true` and the audience `onboarding:verified` hold for them. Other plugins add steps between, such as [Interests](../interests/index.md).

```yaml
# plugins/onboarding/settings.yml
triggers:
  member-join: true
  startup: true
  member-cleared: true
deadline:
  enabled: true
  after: 1d
  action: kick
verified-role: "123456789012345678"
```

| Setting | Meaning |
| --- | --- |
| `triggers.member-join` | a member who joins starts onboarding |
| `triggers.startup` | every member not verified yet starts it when the plugin starts |
| `triggers.member-cleared` | a member whose profile is cleared with `/yui clear` is no longer verified and starts again |
| `deadline` | after `after`, an unfinished onboarding ends; `action: kick` kicks the member, `nothing` leaves them unverified |
| `verified-role` | the role verified members hold |

Being verified is kept in Onboarding's own table, not in the role: a member who leaves and rejoins gets the role back without onboarding again, and a role taken away by hand stays away until the fact changes.

`/yui verify <user>` starts onboarding for a member by hand; by default only moderators may. The audit entries are `onboarding.started`, `.completed`, `.failed`, `.abandoned` (the member left) and `.timeout`.

The texts are in `plugins/onboarding/messages/<locale>/`: `journey.yml` holds what the member reads, including where your rules are, so each language can point to its own channel:

```yaml
# plugins/onboarding/messages/de/journey.yml
end:
  description:
    - "Danke {mention}, du bist verifiziert."
    - "Lies bitte die Regeln in <#123456789012345678>."
```

`settings.yml` in that folder holds the kick reason (`deadline.kick`).
