# Skylark web workspace

Shared frontend packages and the `permission-app` (`permission-pc`) app are linked here with **pnpm workspaces**.

This directory is self-contained so you can later **move `web/` to its own Git repository** and point `pnpm-workspace.yaml` at wherever `permission-pc` lives (monorepo submodule, separate checkout, or published packages).

## Install

From this directory:

```bash
pnpm install
```

## Build admin UI

```bash
pnpm run build:permission-app
```

## Layout

- `packages/oauth-client` — OAuth2 (authorization code) + token storage + axios helper
- `packages/tenant-client` — tenant resolution by domain
- `packages/authz-vue` — Vue permission directive + menu `permlabel` cache helpers
- `apps/skylark-demo-web` — minimal Vue CLI app template (new business UIs can be generated from it)
- `../permission-pc` — admin SPA (`permission-app`)

## New app from template (PowerShell)

From repo root:

```powershell
.\tools\new-frontend.ps1 -AppName order-web -Port 9531 -Title "Order Web"
pnpm install
pnpm --filter order-web run serve
```

To scaffold **service + web** together:

```powershell
.\tools\new-fullstack.ps1 -ServiceName order-service -ArtifactId skylark-order-service -WebAppName order-web -BackendPort 18081 -FrontendPort 9531 -Title "Order Web"
```

(`-WebAppName`, `-ArtifactId`, ports, and `-Title` are optional; omit `-WebAppName` to use `order-service-web`.)

## Note on old root `node_modules`

If you previously ran `pnpm install` from the repository root, you may have a leftover `node_modules` at the repo root. It is safe to delete; installs should be run from `web/` from now on.

## Docker / Compose build (`permission-app`)

`docker-compose.yml` builds `permission-app` with **context = repository root** and `dockerfile: permission-pc/Dockerfile`, so the image can see both `web/packages/*` and `permission-pc/`. Do not switch the build context back to `./permission-pc` only, or workspace dependencies will not resolve.
