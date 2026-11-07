# Yui rewrite plan

The decisions for rewriting Yui to the standards of anvil and Identica, and the order of the work.
Breaking changes are allowed wherever they remove a workaround or give a clearer structure.

## Decided

| Topic | Decision |
| --- | --- |
| Wiring | Spring Boot stays. No feature model. |
| Plugins | Live in this repository under `plugins/`. Only Verification moves in; Synapse and Blabber are dropped. YuiArcadeya stays separate on `repository.arcadeya.com`, in a repository renamed to `arcadeya/yui`. |
| Deployment | In the cluster, code and deployment in separate repositories, as `arcadeya/stalwart` and `arcadeya/stalwart-deploy`: `arcadeya/yui` builds the image, `arcadeya/yui-deploy` records it and holds the configs. Plugins are built into the image. |
| Organisation | Both Arcadeya repositories stay in the Forgejo organisation `arcadeya`, beside the other tools it runs (`stalwart`, `oneuptime`). |
| Users | `Fluctlight` keeps its name. |
| Plugin runtime | One classloader and one child Spring context per plugin, with hot reload. A plugin declares its exported packages in `plugin.json`; reloading a plugin reloads its dependents in dependency order. |
| Database | Hibernate stays; H2 (default), PostgreSQL or MariaDB chosen in config. Flyway migrations, one history table per owner. `ddl-auto=validate`. |
| Servers | One Discord server. |
| Configuration | Configura 2.1, one provider per file, no global state. Old files in `app/` are upgraded on first start. |
| Libraries | Attache 1.2 loads them at runtime; a plugin's libraries go into its own classloader. |
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

Needs in Attache: resolve libraries into a classloader the caller provides.

## Users

- `Fluctlight` becomes an immutable snapshot (`record`). No static services, no `FluctlightStateUpdater`.
- `FluctlightService` changes a user in one transaction: load, change, save. Defaults (primary language) are set
  on creation.
- A bounded cache, invalidated when a change is saved. Users unknown to JDA's cache are loaded from Discord,
  not rejected.
- Plugins keep their own per-user data in their own tables, through their own migrations. In-memory
  extensions on the user go away.
- The first migration fixes `additionalLanguages` (`@ManyToMany`), stops creating `yui_roles` rows as a side
  effect, and drops `@Data` on entities.

## Events

- Each change has a cancellable before event, whose listeners may only veto, and an after event published
  once the transaction commits (`@TransactionalEventListener`), where side effects belong.
- Before events carry the new value (`LanguageChangeEvent` gets the new locale).
- A failing after listener is logged and does not undo the change.
- Plugin lifecycle events: a plugin's commands and listeners are registered after its context started and
  the enable event was not cancelled; unregistered after the disable event was not cancelled.

## Permissions

- One requirement evaluator for commands, components and journeys, covering every interaction type.
- Plugins can add requirement types; they are removed on unload.
- Scope and channel type become one requirement; ids are compared for equality only.
- Role sync states per role which side wins (Yui or Discord), runs once at startup, and does not trigger
  itself. The startup order fails on an unknown task name instead of ignoring it.

## Configuration

- Each file has its own provider with its own path and defaults, as Identica's `ConfigProvider`.
- Adapters and templates are registered on a Configura instance, not on static state, so a plugin's
  classloader brings its own.
- Config files may be read-only (a ConfigMap): Yui never writes them. A missing value takes its default,
  an invalid file stops startup.
- Values can name environment variables (`${DISCORD_TOKEN}`), so secrets stay out of the files.
- Old files are upgraded by a command (`yui config upgrade <dir>`), run on the repository that holds them.
- Configs reload from their folder without a restart.

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

- Read the live schema from `yui-postgres` (read-only) to write the Flyway baseline.
- Cutover: stop the Compose bot, copy its data (5 tables, about 8 MB) into H2, start the cluster
  deployment. Two bots on one token push each other off the gateway, so never both at once.
- Way back: scale the deployment to 0, start the Compose bot again.
- Afterwards, at the owner's word: retire the Compose setup and its backup timer, and delete the old
  `yui` namespace (`arcadeya-cluster/unmanaged/yui`). The new deployment carries a priority class, so it does
  not hold back `pod-priority.yaml`.

## Order of work

1. Build: conventions, toolkit plugins, catalog on latest majors, `yui-launcher` rename.
2. Attache addition for caller-provided classloaders, released.
3. Database: Flyway baseline from the live schema, H2/PostgreSQL/MariaDB, `validate`.
4. Users and events.
5. Permissions and role sync.
6. Configuration on Configura 2.1 with file upgrades.
7. Plugin runtime with hot reload.
8. JDA 6 and cloud.
9. Verification into `plugins/`, `examples/plugin`.
10. Docs and the rest of AGENTS.md; publishing the distribution. The repository setup after Identica (release
    drafting, labels, templates, Dependabot, workflows) came with step 1.
11. `arcadeya/yui`: rename, split into `plugins/plugin-products`, Dockerfile, `Deliver`.
12. `arcadeya/yui-deploy`, namespace policy in `arcadeya-cluster`, OpenBao paths, Argo CD following it.
13. Cutover into the cluster with a way back.
