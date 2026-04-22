# Skylark service (Maven)

Contains shared Spring Boot starters and the demo business service template.

## Build everything

From this directory:

```bash
mvn -q install
```

## Modules

- `skylark-authz-spring-boot-starter` — local RBAC snapshot + MVC interceptor
- `skylark-datadomain-spring-boot-starter` — tenant + data scope helpers
- `skylark-demo-service` — template app (copy with repo-root `tools/new-service.ps1`, output under `service/<name>/`)

This folder is self-contained so you can move it to a separate Git repository later; keep the same module layout or publish starters to a Maven repo and depend by version.
