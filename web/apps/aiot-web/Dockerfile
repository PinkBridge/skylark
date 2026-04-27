# syntax=docker/dockerfile:1.4
# Build static assets. Build context = **repository root**.
# Lockfile + package.json copies keep `pnpm fetch` cached; install uses `--filter`
# because only one app exists under apps/ here (see pnpm-workspace apps/*).
# Requires Docker BuildKit (default on Docker Desktop / recent engine).
FROM node:20-alpine AS builder
WORKDIR /skylark-frontend

RUN corepack enable && corepack prepare pnpm@9 --activate

# Lockfile lists this importer; pnpm needs the project root to exist.
RUN mkdir -p /permission-pc/vendor/i18n-client /permission-pc/vendor/login-redirect-client
COPY permission-pc/package.json /permission-pc/package.json
COPY permission-pc/vendor/i18n-client/package.json /permission-pc/vendor/i18n-client/package.json
COPY permission-pc/vendor/login-redirect-client/package.json /permission-pc/vendor/login-redirect-client/package.json

COPY web/pnpm-lock.yaml web/package.json web/pnpm-workspace.yaml ./

# Workspace members: manifests only (then full COPY later). Add a line here when you add a package under web/packages/.
COPY web/packages/oauth-client/package.json ./packages/oauth-client/
COPY web/packages/tenant-client/package.json ./packages/tenant-client/
COPY web/packages/i18n-client/package.json ./packages/i18n-client/
COPY web/packages/i18n-client/package.json ./packages/i18n-client/
COPY web/packages/authz-vue/package.json ./packages/authz-vue/
COPY web/packages/admin-shell/package.json ./packages/admin-shell/
COPY web/apps/aiot-web/package.json ./apps/aiot-web/

RUN --mount=type=cache,id=pnpm-skylark-store,target=/pnpm/store \
    pnpm fetch --store-dir /pnpm/store

COPY web/packages ./packages
COPY web/apps/aiot-web ./apps/aiot-web

RUN --mount=type=cache,id=pnpm-skylark-store,target=/pnpm/store \
    pnpm install --frozen-lockfile --store-dir /pnpm/store --filter "aiot-web..."

ARG PNPM_FILTER=aiot-web
ENV NODE_ENV=production

RUN pnpm --filter ${PNPM_FILTER} run build

FROM nginx:1.27-alpine
COPY web/apps/aiot-web/docker/nginx.default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /skylark-frontend/apps/aiot-web/dist /usr/share/nginx/html
EXPOSE 80
