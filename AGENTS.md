# AGENTS.md

Rules for contributing to Yui. The rewrite in progress and its decisions are in `docs/rewrite-plan.md`.

## Contributing

### Branches and pull requests

- Work happens on `feature/...` branches, merged into `dev` through a pull request.
- Pull requests are merged by rebase, so each commit lands on `dev` as it is: keep the history clean, no fixup commits.

### Commits

- Conventional Commits with a scope, a lowercase imperative subject and `!` for a breaking change:
  `refactor(build)!: build with conventions, the toolkit and a Java 25 toolchain`.
- The body explains why, in prose.

### Issues

- A suggestion opens with `suggestion`, the label of an idea not decided on yet. An accepted one trades it for
  `feature` or `change`; a declined one is closed as not planned.
- A bug report opens with `bug`.

### Release Notes

A merged pull request lands in the release draft as `* <title> ([#<number>](<url>))`, under the category of its label.
The draft is written for server owners, members and plugin developers, so the title and the label decide what they
read.

#### Titles

- use the strict format `Area: Description`, with a past-tense description of what changed for the reader:
  `Introduced`, `Changed`, `Fixed`, `Removed`
- describe the effect, not the implementation: `Roles: Fixed roles changed in Discord being taken back`, not
  `Roles: Made the sync one-way per role`
- the draft sorts by the exact area, so spell an area exactly as listed below

Areas for server owners and members:

`Audit`, `Commands`, `Configuration`, `Database`, `Journeys`, `Languages`, `Plugins`, `Roles`, `Updates`,
`Verification`

- reuse an existing area whenever one fits; a new command is `Commands`, a new setting in a plugin's file belongs to
  that plugin's area
- add a new area only for something server owners recognise by that name, such as a new plugin or config file, and
  add it to this list and to the area list of `.github/ISSUE_TEMPLATE/feature_request.yml` in the same pull request
- never use module, package, or class names as an area: no `Common`, `Adapter`, `Launcher`, `Fluctlight`

`API:` is for changes a plugin developer compiles against or listens to: public types, events and the plugin
contract. The drafter moves `API:` changes out of their category into a closing "For developers" section. A change
that matters to both server owners and developers takes the owners' area; mention the API part in the pull request
body.

#### Labels

| Label | Use for | Draft section |
| --- | --- | --- |
| `feature` | something new for server owners, members, or developers | What’s new |
| `change` | changed behavior, configuration, or API | Enhancements |
| `bug` | a fix for a bug that shipped in a release | Fixes |
| `dependencies` | a library update | Dependencies |
| `skip-changelog` | anything a reader of the release would not notice | not listed |

Use `skip-changelog` for:

- internal refactors with no effect on server owners, members, or the public API
- build, CI, test, and documentation-only changes
- a fix for a bug that never shipped: when the broken code was introduced after the last release, the fix belongs to
  the work that introduced it, not to the release notes
- changes to something that was itself added after the last release and never shipped

Add `major` next to the category when server owners or plugin developers must act on upgrade: configuration moved or
reset without an upgrade, stored data that is not carried over, or an incompatible public API change. One `major`
pull request makes the draft a major version.

Plugin labels (`plugin:verification`) mark which plugin a pull request or issue is about; they do not change the
draft.

#### Release notes file

The drafter rebuilds the draft on every push to `dev`, so text typed into the draft is lost. Everything written by
hand, such as the subtitle, the summary, upgrade notes, and security callouts, lives in `.github/release-notes.md` and
is changed through pull requests like any other file.

- the content replaces the subtitle and summary placeholders of the draft only while `version` matches the version the
  draft resolves to; notes of a published release therefore never reach the next draft
- when a change needs a note, add it to this file in the same pull request; if the file names an already published
  version, start the notes of the next release by replacing the whole file and setting `version` to the version the
  draft shows
- keep the heading as `# Update <version> - <subtitle>`
- publishing the draft tags the release, and the release workflow refuses a release that still has the draft's
  placeholders
