# Yui rewrite plan

The decisions for rewriting Yui to the standards of anvil and Identica, and the order of the work.
Breaking changes are allowed wherever they remove a workaround or give a clearer structure.

## Decided

| Topic | Decision |
| --- | --- |
| Wiring | Spring Boot stays. No feature model. |
| Plugin runtime base | Chassis (`whereareiam/chassis`); its gaps for Yui are closed in Chassis. |
| Plugins | Live in this repository under `plugins/`. Only Verification moves in; Synapse and Blabber are dropped. YuiArcadeya stays separate on `repository.arcadeya.com`, in a repository renamed to `arcadeya/yui`. |
| Deployment | In the cluster, code and deployment in separate repositories, as `arcadeya/stalwart` and `arcadeya/stalwart-deploy`: `arcadeya/yui` builds the image, `arcadeya/yui-deploy` records it and holds the configs. Plugins are built into the image. |
| Organisation | Both Arcadeya repositories stay in the Forgejo organisation `arcadeya`, beside the other tools it runs (`stalwart`, `oneuptime`). |
| Users | `Fluctlight` keeps its name. |
| Plugin runtime | One classloader and one child Spring context per plugin, with hot reload. A plugin declares its exported packages in `plugin.json`; reloading a plugin reloads its dependents in dependency order. |
| Database | Hibernate stays; H2 (default), PostgreSQL or MariaDB chosen in config. Flyway migrations, one history table per owner. `ddl-auto=validate`. |
| Servers | One Discord server. |
| Configuration | Configura 2.1, one provider per file, no global state. Old files in `app/` are upgraded on first start. |
| Libraries | Attache 1.3 loads them at runtime; a plugin's libraries go into its own classloader. |
| Commands | Incendo cloud with `cloud-jda6`. Commandant is Minecraft-only. |
| Dependencies | Latest majors: JDA 6, Spring Boot 4.1, Gradle 9.9 and the rest of the catalog. |

## Layout

```text
Yui/
  yui-api/
  yui-common/
  yui-adapter-command/
  yui-adapter-database/
  yui-adapter-plugin/          loads plugin jars
  yui-launcher/                was yui-bootstrap
  plugins/
    plugin-verification/
      src/                     builds the plugin jar
      verification-api/        what YuiArcadeya compiles against
  examples/plugin/             was the YuiPlugin template, built in CI
```

## Plugin runtime

Decided: build it on **Chassis** (`whereareiam/chassis`), which already does discovery, a
class loader per extension over an export layer, dependencies and order, `reload(id)` with dependents, a selection
file for enable and disable, services, cleanup on stop and leak checks. Missing for Yui, to be added in Chassis
rather than worked around: a `chassis-adapter-spring` (child Spring context per plugin), a hook to load Attache
libraries before the main class, and either per-plugin exported packages or accepting that a dependent sees the
whole jar. `plugin.json` would become Chassis's YAML descriptor. Most of `yui-adapter-plugin` would go away.

- Exports (agreed): a plugin lists its exported packages in its Chassis descriptor (`exports:`). A dependent's
  loader gets only classes from those packages from a dependency, never its other classes or its libraries; the
  Spring adapter hands over only beans whose types are exported. A plugin that exports nothing cannot be depended
  on. Built in Chassis, so Socialismus's modules get it too.
- The Spring adapter needs no change to Chassis's core: an `ExtensionFactory` opens a child context with Yui's as
  parent and hands it to `ExtensionContext.onClose`. The Attache step needs a hook in Chassis between creating
  the loader and loading the main class, and a way to add jars to the loader (today `addURL` is protected).
- Attache (agreed): Chassis gets a general `prepare` step in `HostOptions` that runs on each new loader before the
  main class loads, may add jars to it and returns an `AutoCloseable` closed when the extension stops. A new module
  `chassis-adapter-attache` implements it with Attache (`AttacheLibraries.of(...)`): it reads only the extension
  jar's own library descriptor and adds the libraries to its loader. Yui uses that module; libraries stay private.
- Spring (agreed): `chassis-adapter-spring` provides an `ExtensionFactory` that opens a child context with Yui's as
  parent, adds the exported beans of the plugin's dependencies, registers the main class and refreshes. It only
  checks that the main class is a Spring `@Configuration`. Yui's own annotation `@Plugin` in `yui-api` is that
  configuration: it composes `@Configuration` and `@ComponentScan` (`scan` narrows or widens it; empty is the main
  class's package). A main class without it fails to load with the plugin id. Plugins see only Yui's API types,
  through Chassis's export layer, so Yui's internal beans stay out of reach.

Built in Chassis on 9 November 2026 as four stacked draft pull requests, merged into `dev` in order:
whereareiam/chassis#7 (preparation step), #8 (exports), #9 (`chassis-adapter-attache`), #10 (`chassis-adapter-spring`).
Yui's export layer must include `org.springframework`, since Spring's generated configuration classes live in a
plugin's loader.

The design below predates that proposal.

- `plugin.json` holds id, version, dependencies, exported packages and libraries.
- Load order: read descriptors, resolve dependencies, create the classloader (Yui plus the exported
  packages of declared dependencies), load libraries into it with Attache, then refresh the child context.
- Beans whose types are in a dependency's exported packages are handed to the dependent context.
  Nothing is decided by package names or the call stack.
- Everything a plugin registers (commands, definition providers, JDA listeners, component listeners,
  requirement evaluators, scheduled tasks, translations) goes through plugin-scoped services that record
  it and undo it when the plugin unloads.
- Plugin identity (config folder, translation prefix, listener id namespace) comes from a `PluginContext`
  bean built from `plugin.json`.
- Events reach plugins through one ordered bus that Yui owns, not a bridge between multicasters.
- Errors are logged with the plugin's id, never swallowed.

Attache 1.3.0 already loads a plugin's libraries into the plugin's classloader; no change to Attache is needed.
A plugin's loader exposes a public `addURL`, and Yui gives it its own manager:

```java
StandaloneLibraryManager libraries = new StandaloneLibraryManager(logging, librariesPath, ".", pluginLoader, mode);
libraries.setDescriptorSources(List.of(pluginJar.toUri().toURL())); // the jar, not the descriptor inside it
libraries.loadDescriptors();
```

- Only the plugin's own descriptor is read; without `setDescriptorSources` the manager would also read Yui's
  through the parent loader.
- A library a parent loader already has is skipped (`skip-if-present`, the default), so plugins do not load a second
  JDA or Spring.
- A library only the plugin declares lands in its loader, invisible to Yui and other plugins. After the manager and
  the loader are closed, the loader is garbage-collected.
- A plugin asking for another version of a library a parent already has gets that version loaded into its own
  loader, but parent-first delegation still serves the parent's classes. Yui warns about such a mismatch.

Checked on 7 November 2026 with a plugin jar built by Attache's Gradle plugin: commons-lang3 loaded into the
plugin's loader only, `annotations` skipped as present in the parent, the loader collected after closing.

## Users

- `Fluctlight` becomes an immutable snapshot (`record`). No static services, no `FluctlightStateUpdater`.
- `FluctlightService` changes a user in one transaction: load, change, save. Defaults (primary language) are set
  on creation.
- A bounded cache, invalidated when a change is saved. Users unknown to JDA's cache are loaded from Discord,
  not rejected.
- Plugins keep their own per-user data in their own tables, through their own migrations. In-memory
  extensions on the user go away.
- A user holds facts, not roles: the languages they speak, that they finished a journey, what a plugin records
  in its own tables. The stored role list (`yui_fluctlights_roles`, `yui_roles`) goes away; Discord's member
  roles are the only record of roles.
- Roles follow facts one way: the owner of a fact grants or removes its role when the fact changes (core for
  languages, Verification for `verified`, a products plugin for its products), through one core service that
  also logs it. Yui grants them again when a member rejoins. A role removed by hand stays removed.
- Drop `@Data` on entities. (`additionalLanguages` is `@ManyToMany` since the database step.)

## Database

- `persistence.yml` chooses `type`: `H2` (the default, a file at `file` in the data folder), or `POSTGRESQL` or
  `MARIADB` with host, port, database name and credentials. The pool takes `pool`.
- `DatabaseConfiguration` connects to it; `PersistenceConfiguration` runs Flyway, then Hibernate with
  `hbm2ddl.auto=validate`, so Hibernate never changes the tables.
- Migrations live in `db/migration/yui/common` when they read the same on every database, otherwise once per
  database in `db/migration/yui/{h2,postgresql,mariadb}`. Yui's history table is `yui_schema_history`; a plugin's
  is its own.
- `V1__baseline` creates the tables of 1.0.0 cleanly. It is not a copy of the live schema, which Hibernate grew over
  time (a join table with its own id, names left from `yui_profiles`): there is one installation, and its data
  moves into a fresh database at the cutover anyway, so no existing database is patched in place.
- The upgrade moves `database` from an old `settings.yml` into `persistence.yml`. A settings file from before 1.0.0
  has no `database.type`, so the upgrade writes `H2`; an installation that keeps its PostgreSQL server must set
  `POSTGRESQL` before starting 1.0.0 (in the release notes).
- Tests run every persistence test on H2, PostgreSQL and MariaDB, with the real migrations and validation.
- `yui-adapter-database` stays one module, no family (agreed). A plugin's tables get their own Flyway run (its own
  history table), their own Hibernate unit over Yui's pool and their repositories in the plugin's context. The
  contract a plugin declares them with is declarative (a migrations location, an entity package, no JPA types) and
  lives in `yui-api`'s `persistence` package beside Yui's own persistence contracts; it moves into a module of its
  own only when a second kind of storage needs one.
- A plugin declares its tables with its own annotation `@Tables` on the main class, beside `@Plugin` (agreed); the
  plugin runtime knows nothing of it. Defaults: migrations in `db/migration/<plugin id>/` (`common/` plus `h2`,
  `postgresql`, `mariadb`, as Yui's), entities in `<main class package>.entity`, history table
  `<plugin id>_schema_history` with hyphens as underscores. Both locations can be overridden
  (`@Tables(migrations = ..., entities = ...)`). Without `@Tables` a plugin gets no Flyway run and no Hibernate unit;
  with it, a missing migrations folder or entity package stops startup with the plugin id. A plugin's tables carry its
  id as prefix (`verification_journeys`), checked at startup against Hibernate's mapped table names.

## Events

- Each change has a cancellable before event, whose listeners may only veto, and an after event published
  once the transaction commits (`@TransactionalEventListener`), where side effects belong.
- Before events carry the new value (`LanguageChangeEvent` gets the new locale).
- A failing after listener is logged and does not undo the change.
- Plugin lifecycle events: a plugin's commands and listeners are registered after its context started and
  the enable event was not cancelled; unregistered after the disable event was not cancelled.

## Permissions

Requirements come from **Conditia**, a new library (`whereareiam/conditia`, folder `Conditia`) that Yui and
Socialismus share; Socialismus has a near copy of today's Yui system (`RequirementGroup` with six operators).

- One language everywhere: commands, buttons, journey steps, role grants. `requires:` is a list of conditions;
  a list means all, `any` and `not` combine. Cooldown is not a condition but its own block.
- **Audiences** are named lists of conditions (`audiences.yml`); `audience: moderator` reuses one. A plugin's
  audiences carry its id as prefix (`verification:verified`).
- Plugins add condition types in Java (a name, a settings record, what it needs, an async `test`); they and the
  plugin's audiences are removed on unload.
- Every condition type states what it needs (member, interaction), every place states what it supplies; a
  mismatch or an unknown type stops startup with the config path.
- Core's defaults never name a plugin type. An installation tightens them in its own config
  (`/language` requires `audience: verification:verified` on Arcadeya's guild).

| Condition type | From |
| --- | --- |
| `any`, `not`, `audience` | core (Conditia) |
| `permission` (Discord permission), `role`, `user`, `language`, `joined`, `account` | core, about the member |
| `scope` (guild or DM), `channels`, `channel-type` | core, about the interaction |
| `verified` | Verification |
| `owns` | the Arcadeya products plugin |

No `boosting`: Discord's managed "Server Booster" role covers it.

| Audience | Means | From |
| --- | --- | --- |
| `operator` | `permission: MANAGE_SERVER` | core |
| `moderator` | `permission: MODERATE_MEMBERS` | core |
| `verification:verified` | `verified: true` | Verification |
| `products:customer` | verified and owns a product | Arcadeya products |

A command whose audience reduces to one Discord permission is registered with it as default permission, so
Discord hides it; otherwise Yui checks on use and replies with the command's `denied` text.

### Conditia

Conditia gets its own repository now, set up like Chassis, and is designed against Yui and Socialismus from the
start: Socialismus already has a near copy, so the second user exists. Yui uses a published version from its
permissions step on. Yui runs its plugins on Chassis, so `conditia-adapter-chassis` is built with the rest. The API
is settled in its own round before code is written.

```text
Conditia/
  conditia-api/                Condition, ConditionType<T, S>, Conditions, ConditionScope, NamedConditions, context/ (ContextKey, Context,
                               Supply), result/ (Outcome, Reason, Verdict), exception/
  conditia-common/             engine: registry, evaluation, startup checks; any/not; the named-list type; time, date, chance
  conditia-adapter-configura/  conditions read through Configura 2.1 polymorphic types
  conditia-adapter-chassis/    registrations tied to a Chassis extension
  conditia-testing/
  examples/host/
  docs/content/
```

- Java 21, so Paper and Velocity plugins can use it. Published as `me.whereareiam.conditia:api`, `:common`, ...
- Nothing in `api` assumes a Discord member or a Minecraft player; hosts bring their own types as context keys.
- A check is about one subject plus typed context (agreed). A type is `ConditionType<T, S>`: a name, its settings
  class `S`, the context keys it needs beyond the subject, and `holds(T subject, Context context, S settings)`
  returning a `CompletionStage<Outcome>`. Each host picks its subject type once (`Member` in Yui,
  `SocialismusPlayer` in Socialismus). Checking for another participant is a check with them as subject, so
  Socialismus keeps one `permission` type for sender and recipient. Extras such as the interaction or the chat
  message are context keys, replacing Yui's `originalContext` and Socialismus's last message kept as player data.
  Startup fails with the config path when a place does not supply a needed key or a type's subject does not match.
- A check returns the first failure (agreed). `Conditions.check(requires, subject, context)` runs the list in order,
  stops at the first failure and returns a sealed `Verdict`: `Passed`, `Failed(condition, path, Optional<Reason>)`
  or `Broken(condition, path, error)` when a type threw. A failed `any` reports the `any` itself. The kinds stay
  closed so `any` and `not` mean the same in every host; the variety lives in reasons.
- `holds` returns an `Outcome`: `pass()`, `fail()` or `fail(reason)`. A reason is a record of its type
  implementing `Reason` (`OwnsCondition.Missing(List<String> products)`); its name comes from the type and the
  record (`owns.missing`), its values are the record's components. A type declares its `reasons()`, so startup
  checks that each has a text in every locale. Without a reason Yui uses the command's `denied` text.
- Named lists are optional and named by the host (agreed). Conditia offers `NamedConditions`, a registry of named
  condition lists, and a type that refers to one; a host that wants them registers that type under its own key.
  Yui calls them audiences (`audiences.yml`, `audience: moderator`, a plugin's prefixed by its scope);
  Socialismus may call them otherwise or not use them. Conditia itself never says "audience".
- Registrations live in a scope per owner (agreed). The host opens `conditions.scope("<owner id>")` when a plugin
  or module loads; types and named lists registered through it carry the owner's prefix (`verification:verified`)
  and all go when the host closes the scope on unload. A plugin cannot forget one. After a scope closes, every place
  is checked again: a condition that refers to a removed type or list denies everyone and is logged with its path
  until the owner loads again. `conditia-adapter-chassis` only opens and closes a scope per extension.
- No Minecraft family yet: when two hosts on one server software share types, add
  `conditia-platform/platform-paper` or `platform-velocity`, as Chassis has `platform-neoforge`.

## Configuration

- Each file has its own provider with its own path and defaults, as Identica's `ConfigProvider`.
- Adapters and templates are registered on a Configura instance, not on static state, so a plugin's
  classloader brings its own.
- Config files may be read-only (a ConfigMap): Yui never writes them. A missing value takes its default,
  an invalid file stops startup.
- Values can name environment variables (`${DISCORD_TOKEN}`), so secrets stay out of the files.
- Old files are upgraded by a command (`yui config upgrade <dir>`), run on the repository that holds them.
- Configs reload from their folder without a restart.

The folder, agreed file by file (each file's content is shown to the owner before it is built):

```text
app/
  settings.yml          token, guild (id), intents, locale, updater
  persistence.yml       the database: type, its connection, pool
  languages.yml         the languages members declare: name, emoji, role
  audiences.yml         named condition lists
  audit.yml             what is kept, and where each category is posted
  interaction/          commands.yml, conversations.yml, appearance.yml
  messages/
    <locale>/           words.yml (was vocabulary.yml), commands.yml, general.yml, log.yml, ...
  plugins/<id>/         the same shape: settings.yml, audiences.yml, interaction/, messages/<locale>/
```

- Yui speaks a language when `messages/<locale>/` exists; there is no list of them. The code says "messages"
  as well (today `localization`), so one thing has one name.
- `languages.yml` is not about translations: members may speak a language Yui has no texts for.
- `roles.yml` and `guild.yml` go away. A role or channel id lives with its only user and moves up only when a
  second appears (Verification keeps `rules` and the welcome channels per locale in its settings).

`settings.yml`, approved:

```yaml
_version: 1
token: "${DISCORD_TOKEN}"
guild: "856839365938118698"
intents: [...]
locale: "en-US"
updater:
  interval: "1d"
  check: { yui, plugins }
  warn: { updates, localBuilds, devBuilds }   # posted as audit entries of category system
```

`interaction/commands.yml`, in Identica's shape; descriptions, usage, arguments and `denied` are texts in
`messages/<locale>/commands.yml`, because Discord gets them per locale:

```yaml
_version: 1
commands:
  clear:
    enabled: true
    aliases: [clear, purge]
    category: moderation
    requires:
      - audience: moderator
      - scope: guild
      - channel-type: [text, thread]
    cooldown: { enabled: true, duration: 10s, group: moderation }
    hide: false
```

Buttons are configured inside the view that shows them (`buttons: { claim: { requires: [...], cooldown: ... } }`).
In a reply only one member sees, an unusable button is hidden or disabled (`unavailable: hide | disable`); on a
public panel everyone sees it, and a click that fails its requirements gets a private reply.

`audit.yml`: an entry is one line of the trail (who did what to whom, when). Whoever writes an entry files it
under a category in code; plugins need no audit file.

```yaml
_version: 1
store: { enabled: true, keep: 90d }      # saved in the database, searchable with /audit
categories:
  members:    { channel: "..." }
  moderation: { channel: "..." }
  system:     { channel: "1452800701833936906" }
entries:                                 # exceptions only
  member.leave: off
  verification.failed: system
```

`persistence.yml`, approved: one shape per database type, as Identica's, picked by `type` through Configura's
polymorphic types, so a file holds only the fields its database uses. The `database` block leaves `settings.yml`.

```yaml
# H2, the default
_version: 1
type: H2
file: "database/yui"           # in Yui's data folder
pool:
  size: { min: 2, max: 10 }
  timeouts: { connect: 30s, idle: 10m, lifetime: 30m }
```
```yaml
# POSTGRESQL or MARIADB
_version: 1
type: POSTGRESQL
host: "postgres.yui.svc"
port: 5432                     # default per type: 5432, or 3306 for MariaDB
database: "yui"
username: "yui"
password: "${DATABASE_PASSWORD}"
pool:
  size: { min: 2, max: 10 }
  timeouts: { connect: 30s, idle: 10m, lifetime: 30m }
```

`pool` replaces `hikari` and takes Yui's `Duration` instead of milliseconds; the upgrade maps the old flat keys.

Still open, one file at a time: `interaction/conversations.yml`, `interaction/appearance.yml` (palette and embed
styles per kind), `languages.yml` in detail, `audiences.yml` in detail, the message files, Verification's files.

## Build

- `build-logic` conventions after anvil and Identica: `module-java`, `module-api`, `module-plugin`,
  `packaging-publication`; toolkit `versioning`, `publish.maven` and `architecture` (`verifyArchitecture`).
- Publish to `https://registry.whereareiam.me/maven/packages`.
- Unit tests mirror the code; database tests run on H2, and on PostgreSQL and MariaDB with Testcontainers.
- AGENTS.md and Scriptorium docs (`scriptorium.project.json`, `docs/content/`).
- Drop `spring-boot-webflux` unless something needs it; exclude JDA's voice natives if voice is unused.
- A published Gradle plugin for building Yui plugins (`me.whereareiam.yui.plugin`): writes `plugin.json`
  from the build (id, version, dependencies, exports), declares runtime libraries through Attache and packages
  the jar. Used by `plugins/`, `examples/plugin` and `arcadeya/yui`.

## Deployment

This repository stays generic and publishes Yui's distribution and plugins to Maven. The Arcadeya side
is two repositories on `repository.arcadeya.com`, the way `arcadeya/stalwart` and `arcadeya/stalwart-deploy`
are split:

```text
arcadeya/yui                   code; today arcadeya/yuiarcadeya
  plugins/
    plugin-products/           id "arcadeya-products", com.arcadeya.yui.plugin.products
                               (today's products step and /products)
    plugin-<next>/
  Dockerfile                   Liberica JRE 25 + Yui distribution <version> + Verification + every jar of plugins/,
                               libraries resolved at build time
  .forgejo/workflows/deliver.yml
                               a version tag builds the image and opens a pull request in arcadeya/yui-deploy

arcadeya/yui-deploy            what runs, and how
  overlay/prod/
    workload.yaml              Deployment (replicas 1, strategy Recreate, priority class), PVC yui-data, ExternalSecret
    config/                    settings.yml, commands.yml, roles.yml, languages.yml, plugins/<id>/...
    kustomization.yaml         configMapGenerator from config/
  version/prod/                the image digest and the commit of arcadeya/yui it was built from

arcadeya-cluster/namespaces/yui/   namespace, quota, secrets store, pull identity
```

- A release: a version tag in `arcadeya/yui` (a new Yui version is a catalog change there), whose pull request
  in `arcadeya/yui-deploy` is merged to deploy it.
- A config change: a pull request in `arcadeya/yui-deploy`, with no image built.

| What | Where |
| --- | --- |
| Yui, plugins, libraries | The image; its digest says what runs |
| Configs | `arcadeya/yui-deploy`, `overlay/prod/config/`, mounted read-only as a ConfigMap; a change restarts the pod |
| Discord token, database password | OpenBao, through the ExternalSecret, as environment variables |
| H2 database and Yui's own H2 backups | PVC `yui-data` |

Plugin hot reload is for development and runs outside Kubernetes; in the cluster a plugin change is a new
image.

Arcadeya plugin ids carry the prefix `arcadeya-`, since they share `plugins/` with Yui's own. Code two
plugins share becomes a plugin of its own that exports its packages, once a second plugin needs it, not
before. `ArcadeyaFeature`, `FeatureBeanFilter`, `ArcadeyaConfigProvider` and the empty `feature-migration`
modules go away.

### From today's setup

Yui and its own PostgreSQL run under Docker Compose on node-worker-1 in `/data/arcadeya/yui` (source:
`/home/cluster/_edge/node-worker-1/yui/` on node-1), a March build. The cluster still holds the old
`yui/postgres-yui` from before 29 October.

- The live schema of `yui-postgres` was read on 7 November 2026: 283 users, 90 additional languages, 771 role
  assignments, 3 languages, 8 roles.
- Cutover: stop the Compose bot, copy its rows into a freshly migrated H2 database, start the cluster
  deployment. Two bots on one token push each other off the gateway, so never both at once.
- Way back: scale the deployment to 0, start the Compose bot again.
- Afterwards, at the owner's word: retire the Compose setup and its backup timer, and delete the old
  `yui` namespace (`arcadeya-cluster/unmanaged/yui`). The new deployment carries a priority class, so it does
  not hold back `pod-priority.yaml`.

## Order of work

1. Build: conventions, toolkit plugins, catalog on latest majors, `yui-launcher` rename.
2. Attache loading into a plugin's own classloader: already in Attache 1.3.0, checked (see Plugin runtime).
3. Database: Flyway with a clean baseline, H2/PostgreSQL/MariaDB, `validate` (see Database).
4. Users and events.
5. Permissions on Conditia; role sync removed (roles follow facts).
6. Configuration on Configura 2.1 with file upgrades.
7. Plugin runtime with hot reload, on Chassis.
8. JDA 6 and cloud.
9. Verification into `plugins/`, `examples/plugin`.
10. Docs and the rest of AGENTS.md; publishing the distribution. The repository setup after Identica (release
    drafting, labels, templates, Dependabot, workflows) came with step 1.
11. `arcadeya/yui`: rename, split into `plugins/plugin-products`, Dockerfile, `Deliver`.
12. `arcadeya/yui-deploy`, namespace policy in `arcadeya-cluster`, OpenBao paths, Argo CD following it.
13. Cutover into the cluster with a way back.
