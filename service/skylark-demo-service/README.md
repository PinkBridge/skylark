# skylark-demo-service

Template/demo Spring Boot business service for Skylark.

## Local run

From the `service/` directory, build the reactor and run the demo module:

```bash
mvn -q install
mvn -q -pl skylark-demo-service spring-boot:run
```

Or from repo root:

```bash
mvn -q -f service/pom.xml install
mvn -q -f service/skylark-demo-service/pom.xml spring-boot:run
```

## Generate a new service from this template (Windows / PowerShell)

From repo root:

```powershell
.\tools\new-service.ps1 -ServiceName order-service -ArtifactId skylark-order-service -Port 18081
```

This creates `service/order-service/` (same parent POM as the starters).

## Endpoints

- `GET /api/demo/ping` (no auth, for smoke test)

## Enable authz / data-domain

Edit `src/main/resources/application.yml`:

- `skylark.authz.enabled=true` and set `app-code`, `permission-base-url`, `client-id`, `client-secret`
- (Optional) `skylark.datadomain.resolve-data-scope=true` and set permission URL + client credentials

## Frontend counterpart

Vue CLI template: `web/apps/skylark-demo-web`. Generate a new UI app with `.\tools\new-frontend.ps1` (see `web/README.md`).
