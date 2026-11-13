---
title: Interests
description: Let members pick the topics they are here for and give them each topic's role.
---

Interests adds a step to [Onboarding](../onboarding/index.md), after the languages: the member picks the topics they are here for from a menu, as many as they like or none. Each topic has a role, which opens its channels. Later, `/topics` shows the same menu again.

```yaml
# plugins/interests/topics.yml
development:
  role: "123456789012345680"
minecraft:
  role: "123456789012345681"
```

A topic's name and description are texts, so they read in each member's language:

```yaml
# plugins/interests/messages/en-US/topics.yml
development:
  name: "Development"
  description: "Code, tools and the dev channel"
```

Without a name, the menu shows the topic's id. Without `topics.yml`, the step is skipped.

By default `/topics` needs `audience: onboarding:verified`; change it in `plugins/interests/commands.yml`. A member who leaves and rejoins gets their topics' roles back; `/yui clear` forgets them.
