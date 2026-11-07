# Security Policy

Yui holds a Discord bot's token and acts on a server with the bot's permissions, so a flaw in it or in a plugin
can let someone change roles, read members' data or act as the bot.

## Supported versions

Security fixes go into the next release built from `dev`. Only the latest release receives them; update to it before
reporting.

## Reporting a vulnerability

Report vulnerabilities privately through
[GitHub's private vulnerability reporting](https://github.com/whereareiam/Yui/security/advisories/new). Do not open a
public issue, pull request, or Discord message with the details.

Include:

- the Yui version, plugins, and database
- what an attacker can do, and what they need for it
- steps to reproduce

You receive an answer on the report. The fix is released before the details are published, and the release notes name
it as a security fix.
