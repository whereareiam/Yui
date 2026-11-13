---
version: 1.0.0
---

# Update 1.0.0 - The Rewrite

Yui 1.0.0 is a rewrite to a new structure: plugins get their own classloader and can be reloaded while Yui runs, the database is chosen in the configuration, and the build follows the conventions of the other `whereareiam` projects.

## Upgrading

* Yui stores its data in an H2 file by default. To keep using a PostgreSQL server, set `database.type: POSTGRESQL` in `settings.yml` before the first start of 1.0.0; MariaDB is `MARIADB`. A database from before 1.0.0 is not upgraded in place: its rows move into a database 1.0.0 created.
* The API is published as `me.whereareiam.yui:api` on `https://registry.whereareiam.me/maven/packages`; plugins built against `me.whereareiam:Yui` must be rebuilt.
