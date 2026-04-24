# business-web

Minimal Vue 3 + Vue CLI template aligned with `permission-app` patterns:

- OAuth2 authorization code via `@skylark/oauth-client`
- API client via `createAxiosApi`
- Tenant helpers via `@skylark/tenant-client`
- `v-permission` via `@skylark/authz-vue` (menu tree fetch is a stub — replace in real apps)

## From monorepo root

```powershell
pnpm -C web install
pnpm -C web run serve:demo-web
```

## Generate a new app from this template

```powershell
.\tools\new-frontend.ps1 -AppName order-web -Port 9531 -Title "Order Web"
```
