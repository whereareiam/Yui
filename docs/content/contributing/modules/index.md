---
title: Modules
description: What each module of Yui owns and which way they depend on each other.
---

```text
yui-api/                 what plugins compile against: contracts, models, events, the plugin annotations
yui-common/              the standard implementation: configuration, texts, permissions, conversations, journeys,
                         the audit trail, fact roles, the plugin parts that register a plugin's beans
yui-adapter-command/     commands on Incendo Cloud and JDA
yui-adapter-database/    Flyway, Hibernate and the persistence of Yui and of plugins' tables
yui-adapter-plugin/      the plugin host on Chassis: loading, the export layer, libraries through Attache
yui-launcher/            starts it all: Yui.jar
plugins/
  plugin-onboarding/     Onboarding, with onboarding-api for other plugins
  plugin-interests/      Interests
examples/plugin/         the example plugin
yui-gradle-plugin/       me.whereareiam.yui.plugin, which builds every plugin
build-logic/             the build's conventions
```

Dependencies point one way: the API depends on nothing of Yui's; `yui-common` implements it; each adapter depends on the API and on common for one technology; the launcher assembles them. Plugins depend on the API alone, and on another plugin only through its `-api`.

Plugins see only `me.whereareiam.yui.api` of Yui, and the libraries the host hands them (Spring, JDA, Conditia, Semantica, Chassis, Jakarta Persistence and annotations, Jackson's annotations). Whatever a plugin needs from Yui therefore belongs in `yui-api`; a plugin that reaches for `yui-common` will not load.

A plugin's beans live in a context of its own beneath Yui's. Yui's `PluginPart`s register what it holds, in order: texts, conditions and audiences, listeners, commands, journeys and events, and undo it the last first when it stops.
