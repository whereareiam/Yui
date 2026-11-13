---
title: Tables
description: Keep a plugin's own data in Yui's database with migrations, entities and Spring Data repositories.
---

A plugin that keeps data of its own, such as which members are verified, declares its tables with `@Tables` beside `@Plugin`:

```java
@Plugin
@Tables
public class Onboarding implements YuiPlugin {
}
```

Before the plugin starts, Yui brings its tables up to date from its migrations, then maps its entities and checks that they match. The plugin's Spring Data repositories and `@Transactional` methods work on its tables only.

```text
src/main/resources/db/migration/onboarding/
  h2/V1__baseline.sql
  postgresql/V1__baseline.sql
  mariadb/V1__baseline.sql
  common/            migrations that read the same on every database
src/main/java/.../onboarding/entity/VerifiedMemberEntity.java
src/main/java/.../onboarding/repository/VerifiedMemberRepository.java
```

```sql
CREATE TABLE onboarding_members (
	user_id BIGINT NOT NULL,
	verified_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_onboarding_members PRIMARY KEY (user_id)
);
```

```java
@Getter
@Entity
@Table(name = "onboarding_members")
@NoArgsConstructor
@AllArgsConstructor
public class VerifiedMemberEntity {
	@Id
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "verified_at", nullable = false)
	private Instant verifiedAt;
}
```

```java
public interface VerifiedMemberRepository extends JpaRepository<VerifiedMemberEntity, Long> {
}
```

- Migrations live in `db/migration/<plugin id>/`, entities in the main class's package `.entity`; `@Tables(migrations = …, entities = …)` names others.
- Every table's name starts with the plugin's id, hyphens as underscores (`arcadeya_products_owners`). The plugin's migrations are recorded in a history table of its own, `<id>_schema_history`.
- A plugin without migrations or entities, or with a table of another name, does not start, and Yui's log says why.
- Write each migration for H2, PostgreSQL and MariaDB, since a server may run any of them. Never change a migration that shipped; add the next one.
