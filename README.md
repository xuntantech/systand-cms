# Systand CMS

`com.systand.cms` is the shared Site/Page/Section CMS used by customer tenants and
Systand's internal sites. It owns its API, business services, MyBatis mappings,
HTTP controllers, error codes, and Spring Boot starter. The Java artifacts have
no dependency on `systand-platform` modules.

## Modules

| Artifact | Contents |
| --- | --- |
| `cms-api` | Tenant, actor, media and translation ports; stable error/page contracts |
| `cms-core` | Domain rules and exceptions |
| `cms-persistence-mybatis` | CMS-owned DOs, mappers and JSON/UUID handlers |
| `cms-application` | Site/Page/Section services, requests and responses |
| `cms-webmvc` | Configurable management HTTP endpoints and error advice |
| `cms-spring-boot-starter` | Auto-configuration; pulls in the modules above |
| `cms-bom` | Version alignment for applications using multiple CMS artifacts |

Java 21, Spring Boot 4.1.x, PostgreSQL and MyBatis-Plus are required. For the
first release candidate, the supported data model is the existing `cms` schema
in `systand-cms-database`. Its DDL still references `tenant.tenant` and, for
section-type audit fields, `sys_auth.sys_user`. These are **database**
dependencies, not Java dependencies. A standalone internal database needs a
separately reviewed schema migration that replaces these foreign keys; do not
run the current platform DDL against a database lacking those schemas.

## Add to a Spring Boot application

In Gradle, configure the private GitHub Maven repository with read-only
package credentials supplied outside source control:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/xuntantech/systand-cms")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull
            password = providers.gradleProperty("gpr.token").orNull
        }
    }
}

dependencies {
    implementation("com.systand.cms:cms-spring-boot-starter:0.3.1")
}
```

Set `gpr.user` and `gpr.token` in user-level Gradle properties. A single
starter dependency exposes the API, services, persistence and controllers
transitively. The BOM is optional when using one explicitly versioned starter;
use it when declaring several CMS artifacts directly.

`0.3.1` currently builds from this checkout; it is **not** available
from GitHub Packages until the release workflow publishes it. The platform
checkout uses a conditional Gradle composite build to consume the sibling CMS
source locally. Other hosts should wait for a published coordinate or use a
source composite build of their own.

## Host ports and configuration

The host must provide one `CmsActorProvider` and one `CmsMediaPort`. The latter
may be an adapter to a media service; the CMS never loads platform-media.
Do not implement a media adapter that silently accepts missing files or drops
bindings. All actor IDs must come from a trusted security context, not an HTTP
request body.

For an internal single-tenant deployment, allocate a stable tenant UUID in
the database and configure:

```yaml
systand:
  cms:
    enabled: true
    web-enabled: true
    api-prefix: /v1/cms
    tenant:
      mode: FIXED
      fixed-id: ${INTERNAL_CMS_TENANT_ID}
```

The starter supplies `CmsTenantProvider` in `FIXED` mode. For customer
multi-tenancy use `CUSTOM` and provide exactly one host bean resolving the
tenant from authenticated request context:

```java
@Bean
CmsTenantProvider cmsTenantProvider(TrustedTenantContext context) {
    return context::requireTenantId;
}
```

Do not use a client-supplied `tenant_id`. Services scope Site/Page/Section
operations to the provider's tenant. The host remains responsible for the
HTTP authentication/authorization policy and for any MyBatis tenant plugin it
chooses to install. `web-enabled: false` keeps CMS services and mappers active
but does not expose CMS controllers. `enabled: false` disables the starter.

The shared management API includes `/sites`, `/sites/{siteId}/pages`, page
locales, sections and section locales beneath `api-prefix`. `POST /sites`
accepts site fields only; tenant and audit user are resolved on the server.
The current `cms.site` schema requires a human audit UUID, so site creation by
system/anonymous actors is rejected until an audit-compatible migration is
available. CMS errors use `CMS_*` codes and a CMS-owned response envelope.

## Verify and publish

```sh
./gradlew clean check publishToMavenLocal
```

The GitHub Actions publish workflow runs on a version-matching tag or manual
dispatch. Run the checks, then tag the same version (`v0.3.1`).
Do not push a tag before checking the database prerequisite for the target
deployment. No package is published merely by editing this repository.
