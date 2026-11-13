---
title: Plugins
description: Add plugins to Yui, switch them on and off, and give them their files.
---

A plugin is a jar in `plugins/`. Yui loads every jar there at startup, after the plugins it needs, and gives each a folder of its own, `plugins/<id>/`, for its files:

```text
plugins/
  plugin-onboarding.jar
  plugin-interests.jar
  onboarding/
    settings.yml
    messages/de/journey.yml
  interests/
    topics.yml
plugins.yml           which plugins are switched off
```

`/yui plugin` lists them with their state: `enabled`, `disabled`, `waiting` for a plugin they need, or `failed` with the reason. `/yui plugin disable <id>` and `enable <id>` switch one off and on and remember it in `plugins.yml`; `reload <id>` stops and starts it again with its files read anew; `rescan` loads jars added since. A plugin's commands, texts, audiences and roles come and go with it.

A plugin cannot reach Yui's own code or another plugin's, only Yui's API and what a plugin it needs exports. Its libraries are downloaded into `libraries/` the first time it starts.

Plugins kept in Yui's repository:

- [Onboarding](../onboarding/index.md): a new member picks their languages and is verified.
- [Interests](../interests/index.md): members pick the topics they are here for.
