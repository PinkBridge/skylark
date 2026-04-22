# Build static assets (VUE_APP_* from build args)
FROM node:20-alpine AS builder
WORKDIR /app

RUN corepack enable && corepack prepare pnpm@9 --activate

COPY pnpm-lock.yaml package.json ./
COPY vendor/i18n-client ./vendor/i18n-client
COPY vendor/login-redirect-client ./vendor/login-redirect-client
RUN pnpm install --frozen-lockfile

COPY babel.config.js vue.config.js ./
COPY public ./public
COPY src ./src

ARG VUE_APP_OAUTH_BASE=/oauth
ARG VUE_APP_API_BASE_URL=/api
ARG VUE_APP_SYSTEM_TITLE="Skylark权限管理"
ARG VUE_APP_CLIENT_ID=permission-web
ARG VUE_APP_CLIENT_SECRET=112233

ENV NODE_ENV=production \
    VUE_APP_OAUTH_BASE=${VUE_APP_OAUTH_BASE} \
    VUE_APP_API_BASE_URL=${VUE_APP_API_BASE_URL} \
    VUE_APP_SYSTEM_TITLE=${VUE_APP_SYSTEM_TITLE} \
    VUE_APP_CLIENT_ID=${VUE_APP_CLIENT_ID} \
    VUE_APP_CLIENT_SECRET=${VUE_APP_CLIENT_SECRET}

RUN pnpm run build

FROM nginx:1.27-alpine
COPY docker/nginx.default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
