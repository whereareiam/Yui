---
title: Building and testing
description: Build Yui, its plugins and the example, and run the tests on every database.
---

Yui builds with Gradle and a Java 25 toolchain, which Gradle downloads when the machine has none:

```bash
./gradlew build
```

`build/libs/Yui.jar` is Yui; each plugin's jar is in its `build/libs/`. The build checks the module layout (`verifyArchitecture`) and runs every test.

Database tests run on H2, and on PostgreSQL and MariaDB through Testcontainers when Docker is available; without Docker those are skipped, not failed. The Gradle plugin that builds plugins is its own build, `yui-gradle-plugin/`:

```bash
cd yui-gradle-plugin && ../gradlew test
```

To try a change, start `Yui.jar` with `-Dapp.dir=` pointing at a folder with a test server's `settings.yml`, and copy the plugin jars into its `plugins/`.
