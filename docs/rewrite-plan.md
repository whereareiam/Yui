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
| Plugin runtime | On Chassis: one classloader and one child Spring context per plugin, with hot reload. A plugin declares its exported packages in `yui-plugin.yml`; reloading a plugin reloads its dependents in dependency order. |
| Database | Hibernate stays; H2 (default), PostgreSQL or MariaDB chosen in config. Flyway migrations, one history table per owner. `ddl-auto=validate`. |
| Servers | One Discord server. |
| Configuration | Configura 2.1, one provider per file, no global state. No migrations and no compatibility bridges (owner, 13 November 2026): the one installation's files are written anew in the new shape. |
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

Chassis today (13 November 2026, `dev-379d949`) has all of it: exports in the descriptor, `chassis-adapter-attache`,
`chassis-adapter-spring`, host fields, transformers, the control API (`enable`, `disable`, `reload`, `rescan`), the
selection file and leak checks. Yui builds on it as it is:

- The kind is `YuiPlugin extends Extension<ExtensionContext>`; a plugin's main class carries Yui's `@Plugin`
  (`@Configuration`, `@ComponentScan`, `@Import(PluginAttachment.class)`). `PluginAttachment`, a bean in every
  plugin's context, hands that context to Yui while it starts; Yui registers what the plugin holds (JDA listeners,
  component listeners, definition providers and commands, texts as owned layers, a Conditia scope) and hands the
  undoing to `ExtensionContext.onClose`, which Chassis runs before the plugin's Spring context closes.
- Descriptor `yui-plugin.yml` in the jar (Chassis's fields plus Yui's own: `updater`); `plugin.json` goes.
- Folder `plugins/`, data `plugins/<id>/`, selection file `plugins.yml`; texts in `plugins/<id>/messages/<locale>/`.
- The export layer is Yui's API and what it hands plugins: `me.whereareiam.yui.api`, `me.whereareiam.chassis`,
  `me.whereareiam.conditia`, `me.whereareiam.semantica`, `org.springframework`, `net.dv8tion.jda`, and for a
  plugin's tables and models `jakarta.persistence`, `jakarta.annotation` and Jackson's annotations.
- `/plugin` follows Chassis's states and actions: enabled, disabled, waiting, failed; enable, disable, reload,
  rescan. Yui's own loader (`yui-adapter-plugin`'s manager, scanner, class loaders, bean bridge) and its plugin
  events go.

## Users

- `Fluctlight` becomes an immutable snapshot. No static services, no `FluctlightStateUpdater`.
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

Built on 11 November 2026 (step 4):

- `Fluctlight` is a final class with final fields and a builder (`toBuilder`), not a `record`: the Java conventions
  keep top-level models as classes and records for small inner carriers. It holds the JDA `User`, the primary
  language and a set of additional languages; `speaks(language)` asks both.
- `FluctlightService`: `get(id)` (cache, then the database, then Discord through `retrieveUserById`; a new user is
  stored with the default language and announced with `FluctlightCreatedEvent`), `changePrimaryLanguage`,
  `addAdditionalLanguage`, `removeAdditionalLanguage` and `clear`. Each change loads the stored facts again, so a
  stale snapshot changes nothing it did not ask for, and returns the new snapshot. `getOrCreate`, `exists`, `save`,
  `FluctlightRegistry`, `FluctlightData`, `FluctlightExtension` and the queued persistence are gone.
- `FluctlightPersistence` stores `Facts(primaryLanguage, additionalLanguages)` with `load` and `save`, in the
  caller's transaction.
- The cache keeps the 10 000 users read last and drops a changed user when the change commits, or when the member
  leaves.
- The stored role list is gone (tables, entity, `RoleService`, `ManagedRole`, `roles.yml`), and with it the role
  sync, ahead of step 6. Language roles follow the languages through `LanguageRoleRule`, a transactional listener
  that gives or takes the role `languages.yml` assigns; a member who joins gets the roles of the languages they
  speak. The one core service that grants roles for every fact and logs them comes with step 6.

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
- `persistence.yml` replaces the old `database` block of `settings.yml`; nothing reads the old block. An installation
  that keeps its PostgreSQL server writes `type: POSTGRESQL` there (in the release notes).
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

Built on 11 November 2026 for users (step 4): a before event is named in the present (`FluctlightLanguageChangeEvent`,
`FluctlightAdditionalLanguageAddEvent`, `...RemoveEvent`, `FluctlightClearEvent`) and an after event in the past
(`...ChangedEvent`, `...AddedEvent`, `...RemovedEvent`, `FluctlightClearedEvent`, `FluctlightCreatedEvent`). A change
that changes nothing publishes neither. The role events and `FluctlightUpdatedEvent` are gone. `spring-tx` is part of
`yui-api`, so plugins can listen with `@TransactionalEventListener`.

## Permissions

Requirements come from **Conditia**, a new library (`whereareiam/conditia`, folder `Conditia`) that Yui and
Socialismus share; Socialismus has a near copy of today's Yui system (`RequirementGroup` with six operators).

- One language everywhere: commands, buttons, journey steps, role grants. `requires:` is a list of conditions in
  Conditia's form (below). Cooldown is not a condition but its own block.
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

Conditia has its own repository (`whereareiam/conditia`, folder `Conditia`, MIT like Chassis, because both should
spread), set up like Chassis and designed against Yui and Socialismus from the start: Socialismus already has a near
copy, so the second user exists. Yui uses a published version from its permissions step on. Yui runs its plugins on
Chassis, so `conditia-adapter-chassis` is built with the rest. The API was settled on 12 November 2026.

```text
Conditia/
  conditia-api/                ConditionType (Plain, Single, Pair), Conditions, ConditionScope, Place, Part,
                               Situation, Requirements, Node, SettingsReader, result/ (Outcome, Reason, Verdict),
                               exception/
  conditia-common/             the engine: registrations, binding with startup checks, evaluation; any, not,
                               participant blocks, named lists; time, date, chance
  conditia-adapter-configura/  reads `requires:` and settings through Configura 2.1 (Jackson)
  conditia-adapter-chassis/    a scope per Chassis extension
  conditia-testing/
  docs/content/
```

- Java 21, so Paper and Velocity plugins can use it. Published as `me.whereareiam.conditia:conditia-api`,
  `conditia-common`, ..., like Chassis's artifacts.
- Nothing in `api` assumes a Discord member or a Minecraft player; hosts bring their own types.
- **Typed parts (agreed).** A place names its parts, each typed: `Part<SocialismusPlayer> SENDER`,
  `Part<Member> TARGET`, `Part<Interaction> INTERACTION`; the first is the main participant. A check gets a
  `Situation` with a value for each part. A condition type is a class implementing one of three shapes, so its
  code gets plain typed parameters and never looks anything up:
  - `ConditionType.Plain<S>`: `holds(S settings)`, about no part (`time`, `date`, `chance`);
  - `ConditionType.Single<T, S>`: `holds(T subject, S settings)` (`permission`, `role`, `channel-type`);
  - `ConditionType.Pair<A, B, S>`: `holds(A first, B second, S settings)` (`nearby`, `above`).

  A type has a name, its settings class (a record), optionally its `main()` setting and the `reasons()` it gives.
  Several participants are just several parts: one `permission` type serves sender and recipient.
  This replaces Yui's `originalContext` and Socialismus's last message kept as player data.
- **Which part a condition is about.** A `Single` takes the main part when its type fits, else the only part of
  its type; a `Pair` takes the first fitting part and the next fitting one. Inside a participant block the block's
  part comes first. `of: recipient` (or `of: [sender, recipient]`) names them where that is ambiguous. Binding
  fails with the config path when no part fits, `of` names an unknown part, a part's type does not match or a
  part's name is also a type's name.
- **Config, Form C (agreed).** A list means all; `any` and `not` combine; a bare value fills the type's one
  setting (or its `main()`); a part's name opens a block whose conditions are about that part:

  ```yaml
  requires:
    - permission: chat.global
    - recipient:
        - permission: chat.receive
        - not: { permission: chat.deaf }
    - nearby: 50
  ```

  ```yaml
  # Yui, /clear
  requires:
    - audience: moderator
    - target:
        - not: { audience: moderator }
    - above: [actor, target]
  ```
- A check returns the first failure (agreed). `place.check(situation)` runs the list in order, stops at the first
  failure and completes with a sealed `Verdict`: `Passed`, `Failed(path, condition, Optional<Reason>)` or
  `Broken(path, condition, error)` when a type threw. A failed `any` reports the `any` itself. The kinds stay closed
  so `any` and `not` mean the same in every host; the variety lives in reasons.
- `holds` completes with an `Outcome`: `pass()`, `fail()` or `fail(reason)`. A reason is a record of its type
  implementing `Reason` (`OwnsCondition.Missing(List<String> products)`); its key comes from the type and the
  record (`owns.missing`) and it gives its placeholder values. A type declares its `reasons()`, so a host checks at
  startup that each has a text in every locale. Without a reason Yui uses the command's `denied` text.
- Named lists are optional and named by the host (agreed). The host gives the word that refers to one
  (`audience` in Yui); Conditia itself never says "audience". A list refers to the place's parts like any other
  conditions, so `target: [audience: moderator]` asks it of the target.
- Registrations live in a scope per owner (agreed). The host opens `conditions.scope("<owner id>")` when a plugin
  or module loads; types and named lists registered through it carry the owner's prefix (`verification:verified`)
  and all go when the host closes the scope on unload. A plugin cannot forget one. After any change every place is
  bound again: a condition that refers to a removed type or list denies everyone and is logged with its path until
  the owner loads again. A host creates its places after its plugins have started, so a place that cannot be bound
  stops startup. `conditia-adapter-chassis` only opens and closes a scope per extension.
- No Minecraft family yet: when two hosts on one server software share types, add
  `conditia-platform/platform-paper` or `platform-velocity`, as Chassis has `platform-neoforge`.

**Built on 12 November 2026** (`whereareiam/conditia`, `dev`, 51 tests): the API above, the engine, both adapters,
`conditia-testing` and the docs. Details settled while building:

- Registering is `ConditionRegistrar` (`register`, `name`, `forget`), shared by `Conditions` (plain names) and
  `ConditionScope` (prefixed, closeable). `forget` lets Yui reload `audiences.yml`.
- A type without settings returns `NoSettings.class`; its value names its parts (`above: [actor, target]`).
- A single value fills a list setting (`permission: chat.global`), through Jackson's single-value-as-array.
- `Verdict.Failed.key()` gives the reason's text key; `Reason.values()` its placeholder values (no reflection).
- A failed named list reports its reference (`audience moderator`) with the inner condition's reason.
- `Place` is closeable, so a host closes the places of a config it reloads.
- Published as `dev-e42587f` (12 November 2026); the registry rule `whereareiam-conditia-github` lets its
  GitHub jobs publish, as Chassis's. Yui's step 6 depends on that version.

## Configuration

- Each file has its own provider with its own path and defaults, as Identica's `ConfigProvider`.
- Adapters and templates are registered on a Configura instance, not on static state, so a plugin's
  classloader brings its own.
- Config files may be read-only (a ConfigMap): Yui never writes them. A missing value takes its default,
  an invalid file stops startup.
- Values can name environment variables (`${DISCORD_TOKEN}`), so secrets stay out of the files.
- Nothing upgrades old files and nothing reads an old shape (owner, 13 November 2026): files carry no `_version`, and
  the one installation's files are written anew in `arcadeya/yui-deploy`.
- Configs reload from their folder without a restart.

The folder, agreed file by file (each file's content is shown to the owner before it is built):

```text
app/
  settings.yml          token, guild (id), intents, locale, updater
  persistence.yml       the database: type, its connection, pool
  languages.yml         the languages members declare: emoji, role
  audiences.yml         named condition lists
  audit.yml             what is kept, and where each category is posted
  commands.yml          which commands are on and who may use them
  conversations.yml     where temporary channels go, and how each purpose talks to members
  style/                palette.yml, embeds.yml; later emojis.yml, components.yml, one file each
  messages/
    <locale>/           a text file per config file it speaks for, plus words.yml
  plugins/<id>/         the same shape: settings.yml, audiences.yml, commands.yml, messages/<locale>/
```

- Yui speaks a language when `messages/<locale>/` exists; there is no list of them. The code says "messages"
  as well (today `localization`), so one thing has one name.
- `languages.yml` is not about translations: members may speak a language Yui has no texts for.
- Yui serves one guild, forever (owner, 12 November 2026), so the folder stays flat: no `guild/` or
  `guilds/<name>/`, and no guild key on services, facts or events. `interaction/` is gone as well; a folder
  exists only where its files share a subject (`style/`, `messages/`, `plugins/`).
- `roles.yml` and `guild.yml` go away. A role or channel id lives with its only user and moves up only when a
  second appears (Verification's rules channel lives in its texts, per locale).

`settings.yml`, approved:

```yaml
token: "${DISCORD_TOKEN}"
guild: "856839365938118698"
intents: [...]
locale: "en-US"
updater:
  interval: "1d"
  check: { yui, plugins }
  warn: { updates, localBuilds, devBuilds }   # posted as audit entries of category system
```

`commands.yml` (top level, owner 12 November 2026), in Identica's shape; descriptions, usage, arguments and `denied` are texts in
`messages/<locale>/commands.yml`, because Discord gets them per locale:

```yaml
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
type: H2
file: "database/yui"           # in Yui's data folder
pool:
  size: { min: 2, max: 10 }
  timeouts: { connect: 30s, idle: 10m, lifetime: 30m }
```
```yaml
# POSTGRESQL or MARIADB
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

`pool` replaces `hikari` and takes Yui's `Duration` instead of milliseconds.

Conversations, decided 13 November 2026:

- A conversation is Yui talking privately with members for a purpose (`"verification"`). It happens in one of
  three types: a DM, a **private thread** or a temporary channel. Discord archives an idle thread itself and keeps
  it readable, and a server holds 1000 active threads; a channel has permissions of its own, 50 per category.
- Configured once, in core's `conversations.yml`: what each type needs (the threads' parent channel, the channels'
  categories) and how a conversation behaves by default (the types tried in order, staff, mention, close,
  limits, transcript). Plugins have no conversations file.
- A plugin overrides any of it through the API for its own use case
  (`conversations.open("verification", members, options)`): what it leaves out comes from the file.
- Members opening a purpose themselves (command, panel, `requires`) can come later without reshaping the file.
- A thread has no permissions of its own: staff see private threads through Manage Threads on the parent
  channel, or Yui adds them by name.

`conversations.yml`, approved. `types` says what each type needs and whether this server allows it at all
(`enabled: false` turns it off for plugins too); `defaults` is what an API call leaves out. `via` is the order the
enabled types are tried; a call whose types are all disabled fails with a clear error. `thread.archive` takes
Discord's durations (1h, 24h, 3d, 1w); `thread.invitable` is Discord's flag for members adding others.

```yaml
types:
  direct:
    enabled: true
  thread:
    enabled: true
    parent: "789…"
    archive: 1h
    invitable: false
  channel:
    enabled: true
    categories: ["123…", "456…"]
    purge: true
defaults:
  via: [direct, thread, channel]
  mention: true
  staff: []
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

`style/`, approved: `palette.yml` names the colours, `embeds.yml` gives each kind (`StyleKit.embeds().error()`) a
colour, plus icons, links and a timestamp. A colour is a palette name (`blue`) or a hex (`"#E03131"`): the `#`
decides, and palette names (`[a-z][a-z0-9-]*`) never start with one. In code it is the sealed `StyleColor`
(`Named(name)` or `Hex(rgb)`), which the palette resolves; `StyleColor.named("violet")` and
`StyleColor.hex("#8A5CF6")` work wherever a colour is taken. An unknown name or a malformed hex stops startup
with its path. Unset values are left out instead of `""`. The author
name and the footer text are words, so they move to `messages/<locale>/`. Plugins' embeds and later
`components.yml` or `emojis.yml` refer to the same palette.

```yaml
# style/palette.yml
brand: "#B57E3F"
sand: "#E6CBA8"
green: "#00FF00"
amber: "#FFAA00"
red: "#FF4C4C"
blue: "#00AAFF"
```

```yaml
# style/embeds.yml
primary:
  color: brand
  footer:
    icon: "https://…"
  timestamp: true
secondary:
  color: sand
success:
  color: green
warning:
  color: amber
error:
  color: "#E03131"
info:
  color: blue
```

`languages.yml`, approved: a language is offered when it is listed (no `enabled`), with its emoji and,
optionally, the role members who speak it get. The `languages:` wrapper goes, and the rendering caches
(`TranslationSettings`) leave the file as code defaults. A language's name is a word, shown in the viewer's
language from `messages/<locale>/languages.yml`; where a locale has no name for it, Discord's own name
(`DiscordLocale.getNativeName()`) is shown.

```yaml
# languages.yml
en-US:
  emoji: "🇺🇸"
  role: "123…"
de:
  emoji: "🇩🇪"
  role: "456…"
```

```yaml
# messages/en-US/languages.yml
en-US: "English"
de: "German"
```

`audiences.yml`, approved: a name maps straight to its list of conditions in Conditia's form. A plugin's file
writes bare names (`verified`); the prefix comes from its folder (`verification:verified`), so a plugin cannot
claim another's name. An installation tightens a plugin's audience in that plugin's file under
`app/plugins/<id>/`. An audience may refer to another; cycles and unknown names stop startup with their path.

```yaml
# audiences.yml
operator:
  - permission: MANAGE_SERVER
moderator:
  - any:
      - audience: operator
      - permission: MODERATE_MEMBERS
```

```yaml
# plugins/verification/audiences.yml
verified:
  - verified: true
```

Messages, approved: texts come from **Semantica 0.1.0** (`me.whereareiam.semantica:*`, replacing the old
`me.whereareiam:semantica:dev-ffdb76d`). A text file mirrors the config file it speaks for, and the path of a file
is the prefix of its keys (`messages/en-US/commands.yml` → `commands.clear.description`):

```text
messages/<locale>/
  commands.yml         descriptions, usage, arguments, denied, replies, errors
  conversations.yml    thread and channel name, first message, close warning
  audit.yml            one line per entry kind
  languages.yml        each language's name
  embeds.yml           author name, footer text per kind
  words.yml            words the others refer to: cancel, confirm, yes, no, back, next
messages/all/          texts that are the same in every language
```

- ICU MessageFormat replaces `<p:name>`: `{count}`, plurals per language, references such as `{@words.cancel}`.
- Message files hold only strings.
- `general.yml` and `log.yml` go: the close warning belongs to conversations, and log was a second name for audit.
- The folder stays `messages/`: in Yui `languages.yml` means the languages members speak.
- Each plugin's texts are an owned layer (`Layer.owned("<id>", ...)`), removed with the plugin. Its keys carry
  its id (`verification.commands.*`) through a key prefix that `FileSource` needs first (Semantica follow-up).
- Discord's markdown is Yui's: a Discord format escapes the values code inserts (a member named `**x**` is shown
  as written), either in Yui or as a Semantica format.
- An organisation's translations repository (Weblate, `HttpSource`) can be layered on top later.

```yaml
# messages/en-US/commands.yml
clear:
  description: "Delete recent messages"
  usage: "/clear <amount>"
  arguments:
    amount: "How many messages to delete"
  denied: "Only moderators can clear messages."
  replies:
    done: "Deleted {count, plural, one {# message} other {# messages}}."
```

Verification's `settings.yml`, approved. `conversation` leaves for core's
`conversations.yml` and the API; `triggers` names the events that start a verification; `deadline` is the time a
member has to finish and `action` what happens after it, a closed set (`kick`, `nothing`; more later); the
member gets `verified-role` for the fact `verified`, through core's role service.

```yaml
# plugins/verification/settings.yml
triggers:
  member-join: true
  startup: true
  member-cleared: true
deadline:
  enabled: true
  after: 1d
  action: kick
verified-role: "…"
```

```text
plugins/verification/
  settings.yml
  audiences.yml          verified
  commands.yml           verify
  messages/<locale>/
    commands.yml         /verify texts
    journey.yml          welcome, additional languages, end
    audit.yml            started, completed, failed, abandoned, timed out
    settings.yml         the text for the deadline's action (the kick reason)
```

The rules channel lives in the text, per locale, so each language can point to its own rules
(`plugins/verification/messages/de/journey.yml`: `"Lies bitte die Regeln in <#456…>."`); settings have no
`rules` key.

Every config file is agreed (13 November 2026); step 5 builds them.

## Build

- `build-logic` conventions after anvil and Identica: `module-java`, `module-api`, `module-plugin`,
  `packaging-publication`; toolkit `versioning`, `publish.maven` and `architecture` (`verifyArchitecture`).
- Publish to `https://registry.whereareiam.me/maven/packages`.
- Unit tests mirror the code; database tests run on H2, and on PostgreSQL and MariaDB with Testcontainers.
- AGENTS.md and Scriptorium docs (`scriptorium.project.json`, `docs/content/`).
- A published Gradle plugin for building Yui plugins (`me.whereareiam.yui.plugin`): writes `yui-plugin.yml`
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
    config/                    settings.yml, commands.yml, languages.yml, plugins/<id>/...
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
- Before the cutover, **remind the owner** that conversations can now use private threads: pick the parent
  channel (the one unverified members already see), give staff Manage Threads on it, and decide whether
  Verification keeps temporary channels or moves to threads.
- Afterwards, at the owner's word: retire the Compose setup and its backup timer, and delete the old
  `yui` namespace (`arcadeya-cluster/unmanaged/yui`). The new deployment carries a priority class, so it does
  not hold back `pod-priority.yaml`.

## Order of work

1. Build: conventions, toolkit plugins, catalog on latest majors, `yui-launcher` rename.
2. Attache loading into a plugin's own classloader: already in Attache 1.3.0, checked (see Plugin runtime).
3. Database: Flyway with a clean baseline, H2/PostgreSQL/MariaDB, `validate` (see Database).
4. Users and events. Built on 11 November 2026 (see Users).
5. Configuration on Configura 2.1; texts on Semantica 0.1.0, after its `FileSource` key prefix
   and with a Discord format (see Messages). Done before permissions (owner, 13 November 2026): the old
   Configura's adapters read single strings only, so `requires:` lists need Configura 2.1 first.
   Built on 13 November 2026: Configura 2.1 with Yui's value types as a Jackson module; `settings.yml`,
   `persistence.yml`, `languages.yml`, `style/`, `audit.yml` and `conversations.yml` in their agreed shape. Yui
   writes none of its files and values may name environment variables. Audit entries are filed under a category in code; channels per locale and wildcard types
   went. Conversations open in direct messages, private threads or temporary channels with staff, idle and
   lifetime closes, limits and transcripts. Texts come from Semantica 0.1.0: English defaults in code, one
   `MessageDefaults` per file, served from memory, with `messages/<locale>/` over them; plugins' texts are owned
   layers prefixed with their id. Nothing has been tried against Discord yet.
   Since done: `audit.yml`'s `store` keeps entries in `yui_audit_entries` for `keep` and `/audit` searches them;
   the embeds' author and footer and the transcript line are texts; the audit texts say `audit.member.*` like the
   entries. `commands.yml` took its final shape with step 6.
6. Permissions on Conditia; one core service that grants roles for facts and logs them (the role sync went with
   step 4).
   Built on 13 November 2026 on Conditia `dev-3090ccb`: commands bind their `requires:` as deferred places with
   the parts `actor` (`Actor`: user, membership, fluctlight) and `interaction` (JDA's); core's types and
   `audiences.yml` as agreed; cooldowns and `hide` work; `FactRoles` gives and takes roles for facts and posts
   `member.role`. Since done: a command whose top-level lines come down to
   Discord permissions alone registers them as its default permissions (an `any` is left to Yui's check); plugins'
   audiences come from `plugins/<id>/audiences.yml`, scoped to the plugin. A command's description,
   example and arguments are texts by key (`commands.<id>.description`, `.example`, `.variables.<argument>`, under a
   plugin's id for its commands); `commands.yml` holds no text and nothing reads `translate(...)`.
7. Plugin runtime with hot reload, on Chassis.
   Built on 13 November 2026 on Chassis `dev-379d949` and Attache `dev-940ee5b`: `PluginHost` runs Chassis with
   `yui-plugin.yml`, Attache libraries, the export layer and `SpringExtensionFactory`; `@Plugin` imports
   `PluginBeans` (an `InteractionService` qualified with the plugin's id, so the call-stack lookup went) and
   `PluginAttachment`, which hands the context to `DefaultPluginRegistrations`: texts, condition types, listeners,
   commands and journeys, each undone on stop. `/plugin`, `/status` and `/update` follow Chassis's states. A test
   starts a real plugin jar, attaches, detaches and checks that its class loader is collected. Since done: all of
   `yui-api` lives under `me.whereareiam.yui.api` (owner's pick, 13 November 2026), so the export layer names that
   one package and a test checks that a plugin cannot load Yui's implementation.
8. JDA 6 and cloud.
   Built on 13 November 2026: JDA 6.7.0 with `cloud-jda6` 1.0.0-beta.4; components come from JDA 6's packages and
   rows (`addComponents(ActionRow.of(...))`); `spring-boot-webflux` and JDA's voice libraries (Opus, Tink) are gone.
9. Verification into `plugins/`, `examples/plugin`.
   Built on 13 November 2026: `plugins/plugin-verification` (with `verification-api`) replaces
   `whereareiam/YuiVerification`, and `examples/plugin` the `YuiPlugin` template; `module-plugin` builds a plugin's
   jar with its own -api and `yui-plugin.yml`. Verification runs the welcome, languages and end steps in a
   conversation that stays open until the journey ends, keeps `verified` in `verification_members` with the role
   following it through `FactRoles` (a rejoining member gets it back), registers `verified` and
   `verification:verified`, posts `verification.started`, `.completed`, `.failed`, `.abandoned` and `.timeout`, and
   kicks after the deadline when `action: kick`. What the plugin runtime lacked came with it: `ConfigReader` and
   `PluginConfigProvider` (a plugin's files, reread on `/reload`), `AudienceDefaults`, `@Tables` with
   `PluginDatabase` (Flyway baselines a plugin's history below its V1, since Yui's tables are there first), Yui's
   events reaching plugins' listeners (Spring hands events up, never down), `ConversationOpenedEvent` and
   `ConversationClosedEvent`, and camel case for every enum of a config file. `yui-api` provides Spring Data JPA to
   plugins. Not tried yet: a plugin jar's entities in its own class loader, against Discord.
10. Docs and the rest of AGENTS.md; publishing the distribution. The repository setup after Identica (release
    drafting, labels, templates, Dependabot, workflows) came with step 1.
11. `arcadeya/yui` (today YuiArcadeya): a rewrite to the same standards, not only a move. Rename, split into
    `plugins/plugin-products`, its config files in rounds with the owner as Yui's were, its `owns` condition and
    `products:customer` audience on Conditia, its texts on Semantica, its facts through core's role service;
    Dockerfile, `Deliver`.
12. `arcadeya/yui-deploy`, namespace policy in `arcadeya-cluster`, OpenBao paths, Argo CD following it.
13. Cutover into the cluster with a way back.
