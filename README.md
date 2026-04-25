## Skylark

English | [简体中文](README.zh-CN.md)

### 1. Overview

Skylark is a **front-end/back-end separated** multi-tenant management system starter. It currently includes an **API gateway**, a **Permission subsystem (OAuth2 + RBAC + Data Domains)**, and **business modules (example + scaffoldable templates)**.

- **Gateway**: `gateway` (Spring Cloud Gateway)
- **Permission service**: `permission` (Spring Boot, OAuth2 Authorization Server + RBAC Resource Server + Data Domains)
- **Admin UI**: `permission-pc` (Compose service name: `permission-app`, Nginx static hosting + reverse proxy)
- **Example business service**: `service/business-service` (Compose service name: `business-service`)
- **Example business web app**: `web/apps/business-web` (Compose service name: `business-web`)
- **Database**: `mysql` (MySQL 8)

### 2. Architecture

#### Topology

```text
Browser
  ├── permission-app (Nginx:80)
  │     ├── /            -> static assets (SPA)
  │     ├── /api/**      -> gateway:80
  │     ├── /oauth/**    -> gateway:80
  │     └── /login/**    -> gateway:80
  │                      └── gateway (Spring Cloud Gateway:80)
  │                            ├── /api/permission/** -> permission:80
  │                            ├── /oauth/**          -> permission:80
  │                            ├── /login/**          -> permission:80
  │                            └── /api/business/**   -> business-service:80
  │
  └── business-web (Nginx:80)
        ├── /            -> static assets (SPA)
        ├── /api/**      -> gateway:80
        ├── /oauth/**    -> gateway:80
        └── /login/**    -> gateway:80

permission (Spring Boot:80) -> mysql:3306
business-service (Spring Boot:80) -> mysql:3306
```

#### Ports & entrypoints

- **Admin UI**: `http://localhost:9527` (override with `PERMISSION_APP_PORT`)
- **Business web app (business-web)**: `http://localhost:9531` (fixed mapping in current Compose)
- **Gateway**: `http://localhost:80` (override with `GATEWAY_PORT`)
- **Permission service**: `http://localhost:19527` (override with `PERMISSION_PORT`)
- **MySQL**: host `3307` -> container `3306` (fixed mapping in Compose)

Note: replace `localhost` with your actual domain name/IP.

#### Path conventions

- `/api/permission/**`: permission APIs (via gateway)
- `/oauth/**`: OAuth2 endpoints (via gateway to permission service)
- `/login`, `/login/**`: login related endpoints (via gateway)

### 3. Tech stack

- **Backend (`permission`)**: Java (target 1.8 / runtime JRE 17), Spring Boot 2.6.x, Spring Security, Spring Security OAuth2 (Authorization Server), JWT, MyBatis, Flyway
- **Gateway (`gateway`)**: Java (runtime JRE 17), Spring Boot, Spring Cloud Gateway (Spring Cloud 2021.0.x)
- **Frontend (`permission-app` / `permission-pc`)**: Vue 3, Vue Router, Vue I18n, Element Plus, Axios, `@element-plus/icons-vue`
- **Database**: MySQL 8.0
- **Delivery**: Docker / Docker Compose, Nginx (static hosting + reverse proxy)
- **Build tools**: Maven (backend), pnpm (frontend, built inside Docker)

### 4. Requirements

- Windows: **Docker Desktop** (includes `docker compose`)
- Linux: **Docker** and **docker compose**
- Optional for local development:
  - Backend: JDK + Maven
  - Frontend: Node.js + pnpm

### 5. Quick start (Docker Compose)

From the repo root:

```bash
docker compose up -d --build
```

The first build may take a while because it downloads Maven/pnpm dependencies.

### 5. Configuration (.env & environment variables)

Common variables used by `docker-compose.yml` (defaults apply when not set):

- **Gateway port**: `GATEWAY_PORT` (default `80`)
- **Permission service port**: `PERMISSION_PORT` (default `19527`)
- **Admin UI port**: `PERMISSION_APP_PORT` (default `9527`)
- **MySQL credentials**: `MYSQL_ROOT_PASSWORD` (default `123456`), `MYSQL_USER` (default `dev`), `MYSQL_PASSWORD` (default `123456`)
- **MySQL init DB**: `MYSQL_DATABASE` (default `skylark_iot`)
- **Permission DB name**: `PERMISSION_DB_NAME` (default `skylark_permission`, with `createDatabaseIfNotExist=true`)

Important: `MYSQL_DATABASE` only applies on the **first initialization** of MySQL data dir. Since Compose mounts `mysql_data:/var/lib/mysql`, changing the variable later won’t recreate/rename the DB. To re-init (data will be lost):

```bash
docker compose down -v
docker compose up -d
```

### 6. Ops commands

```bash
# start/update (detached)
docker compose up -d --build

# status
docker compose ps

# logs
docker compose logs -f permission

# stop & remove containers/networks (keep volumes)
docker compose down

# stop & remove containers/networks/volumes (wipes MySQL data)
docker compose down -v

# rebuild a single service
docker compose build permission-app
```

### 7. Repo layout

- `docker-compose.yml`: local orchestration
- `gateway/`: Spring Cloud Gateway
- `permission/`: permission service (OAuth2 + RBAC + multi-tenant + Flyway)
- `permission-pc/`: admin UI (Vue3 + Element Plus), Compose service name is `permission-app`
- `web/`: pnpm workspace for shared JS packages (`packages/*`) + links `permission-pc` (see `web/README.md`)
- `service/`: Maven reactor for Skylark starters + `skylark-demo-service` template (see `service/README.md`)
- `tools/new-service.ps1`: generate a new business service under `service/<name>/` from the demo template
- `tools/new-frontend.ps1`: generate a new Vue CLI app under `web/apps/` from `skylark-demo-web`
- `tools/new-fullstack.ps1`: run both of the above in one step (same defaults: web app name = `<ServiceName>-web`)

### 8. New app scaffolding & maintenance (PowerShell)

Run scripts from the repo root. They will also keep `docker-compose.yml` and `service/pom.xml` in sync (so you can deploy via Compose without manually editing files).

#### Create a new service (backend)

```powershell
.\tools\new-service.ps1 -ServiceName order-service -ArtifactId skylark-order-service -Port 18081
```

- Output: `service/order-service/`
- Also updates: `service/pom.xml` `<modules>` + `docker-compose.yml` (adds an `order-service:` block)

#### Create a new web app (frontend)

```powershell
.\tools\new-frontend.ps1 -AppName order-web -Port 9531 -Title "Order Web"
```

- Output: `web/apps/order-web/`
- Also updates: `docker-compose.yml` (adds an `order-web:` block)

#### Register the app before developing a new module

Before you start a new business module (especially a new frontend under `web/apps/<app>`), you should **register an application record** in the Permission system:

- **`oauth_client_details`**: create an OAuth client for your new `client_id` (recommended: same as `VUE_APP_CLIENT_ID`).
  - Make sure `web_server_redirect_uri` contains `http(s)://<host>:<port>/home` (comma-separated list is allowed).
- **`sys_oauth_client_meta`**: add `client_id`, `name`, `port`.
  - This is used by the `permission-app` `/apps` landing page to navigate to other apps using the **current domain/IP + meta port**.

#### Create both service + web in one step

```powershell
.\tools\new-fullstack.ps1 -ServiceName order-service -WebAppName order-web -BackendPort 18081 -FrontendPort 9531 -Title "Order Web"
```

If you omit `-WebAppName`, it defaults to `<ServiceName>-web` (e.g. `order-service-web`).

#### Remove a service (cleanup)

```powershell
.\tools\remove-service.ps1 -ServiceName order-service
```

Deletes `service/order-service/`, removes its `<module>` from `service/pom.xml`, and removes its service block from `docker-compose.yml`.

#### Remove a web app (cleanup)

```powershell
.\tools\remove-web.ps1 -AppName order-web
```

Deletes `web/apps/order-web/` and removes its service block from `docker-compose.yml`.

#### Remove both in one step

```powershell
.\tools\remove-fullstack.ps1 -ServiceName order-service -WebAppName order-web
```

Use `-KeepCompose` to keep `docker-compose.yml` unchanged.

### 9. FAQ

#### How do I connect to the database remotely?

MySQL container port `3306` is mapped to host `3307`. From another machine:

- **Host**: your Docker host IP/domain
- **Port**: `3307`
- **User/Password**: `MYSQL_ROOT_PASSWORD` or `MYSQL_USER/MYSQL_PASSWORD`

Example:

```bash
mysql -h <host> -P 3307 -u root -p
```

#### Do I need Node.js installed on the host to build the frontend?

Not necessarily. If you build via Docker (`docker compose build permission-app`), the build runs inside the image. You only need Node.js when you run `pnpm run build/serve` directly on the host.
