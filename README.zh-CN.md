## Skylark

[English](README.md) | 简体中文

### 1. 项目简介

Skylark 是一套**前后端分离**的多租户管理系统，支持快速开发业务的框架。目前包含 **API 网关**、**权限子系统Permission（OAuth2 + RBAC）**。

- **网关**：`gateway`（Spring Cloud Gateway）
- **权限服务**：`permission`（Spring Boot，OAuth2 授权服务器 + RBAC 资源服务）
- **管理端前端**：`permission-pc`（Docker 运行服务名：`permission-app`，Nginx 托管 + 反代）
- **数据库**：`mysql`（MySQL 8）

### 2. 架构说明

#### 整体拓扑

```text
Browser
  └── permission-app (Nginx:80)
        ├── /            -> 静态资源 (SPA)
        ├── /api/**      -> gateway:80
        ├── /oauth/**    -> gateway:80
        └── /login/**    -> gateway:80
                         └── gateway (Spring Cloud Gateway:80)
                               ├── /api/permission/** -> permission:80
                               ├── /oauth/**          -> permission:80
                               └── /login/**          -> permission:80

permission (Spring Boot:80) -> mysql:3306
```

#### 端口与访问入口

- **管理端前端**：`http://localhost:9527`（可用 `PERMISSION_APP_PORT` 覆盖）
- **网关**：`http://localhost:80`（可用 `GATEWAY_PORT` 覆盖）
- **权限服务**：`http://localhost:19527`（可用 `PERMISSION_PORT` 覆盖）
- **MySQL**：宿主机 `3307` → 容器 `3306`（Compose 固定映射）

注意：将 `localhost` 替换为实际的域名/IP。

#### 请求路径约定

- `/api/permission/**`：权限服务业务 API（经网关）
- `/oauth/**`：OAuth2 授权相关（经网关转发至权限服务）
- `/login`、`/login/**`：登录相关入口（经网关）

### 3. 技术栈

- **后端（`permission`）**：Java（编译目标 1.8 / 运行 JRE 17）、Spring Boot 2.6.x、Spring Security、Spring Security OAuth2（授权服务器）、JWT、MyBatis、Flyway
- **网关（`gateway`）**：Java（运行 JRE 17）、Spring Boot、Spring Cloud Gateway（Spring Cloud 2021.0.x）
- **前端（`permission-app` / `permission-pc`）**：Vue 3、Vue Router、Vue I18n、Element Plus、Axios、`@element-plus/icons-vue`
- **数据库**：MySQL 8.0
- **交付与部署**：Docker / Docker Compose、Nginx（静态资源托管 + 反向代理）
- **构建工具**：Maven（后端）、pnpm（前端，Docker 内构建）

### 4. 环境要求

- Window 环境：**Docker Desktop**（含 `docker compose`）
- Linux 环境：**Docker** 和 **docker compose**
- （可选）本地开发：
  - 后端：JDK + Maven
  - 前端：Node.js + pnpm

### 5. 快速开始（Docker 一键启动）

在仓库根目录执行：

```bash
docker compose up -d --build
```

首次构建会下载 Maven / pnpm 依赖，耗时偏长属正常现象。

### 5. 配置说明（.env 与环境变量）

`docker-compose.yml` 中常用变量如下（未设置时使用默认值）：

- **网关端口**：`GATEWAY_PORT`（默认 `80`）
- **权限服务端口**：`PERMISSION_PORT`（默认 `19527`）
- **前端端口**：`PERMISSION_APP_PORT`（默认 `9527`）
- **MySQL 账号**：`MYSQL_ROOT_PASSWORD`（默认 `123456`）、`MYSQL_USER`（默认 `dev`）、`MYSQL_PASSWORD`（默认 `123456`）
- **MySQL 初始化库名**：`MYSQL_DATABASE`（默认 `skylark_iot`）
- **权限服务连接库名**：`PERMISSION_DB_NAME`（默认 `skylark_permission`，并且带 `createDatabaseIfNotExist=true`）

重要提示：MySQL 官方镜像的 `MYSQL_DATABASE` 只在**首次初始化数据目录**时生效。当前 Compose 使用数据卷 `mysql_data:/var/lib/mysql`，因此修改库名后 **重启不会生效**；若需要让初始化库名生效，需要清空卷（会丢数据）：

```bash
docker compose down -v
docker compose up -d
```

### 6. 常用运维命令

```bash
# 启动/更新（后台）
docker compose up -d --build

# 查看运行状态
docker compose ps

# 查看日志
docker compose logs -f permission

# 停止并删除容器/网络（不删数据卷）
docker compose down

# 停止并删除容器/网络/数据卷（会清空 MySQL 数据）
docker compose down -v

# 仅重建某个服务
docker compose build permission-app
```

### 7. 目录结构

- `docker-compose.yml`：本地一键编排
- `gateway/`：Spring Cloud Gateway
- `permission/`：权限服务（OAuth2 + RBAC + 多租户 + Flyway）
- `permission-pc/`：权限管理前端（Vue3 + Element Plus），Docker 服务名为 `permission-app`
- `web/`：pnpm workspace，管理共享前端包（`packages/*`）并链接 `permission-pc`（见 `web/README.md`）
- `service/`：Maven 聚合工程，包含 Skylark 的 Spring Boot starter 与 `skylark-demo-service` 模板（见 `service/README.md`）
- `tools/new-service.ps1`：从 `service/skylark-demo-service` 在 `service/<name>/` 下生成新业务服务
- `tools/new-frontend.ps1`：从 `web/apps/skylark-demo-web` 模板生成新业务前端（`web/apps/<name>`）
- `tools/new-fullstack.ps1`：一次生成前后端（默认前端目录名为 `<ServiceName>-web`）

### 8. 新应用脚手架与维护（PowerShell）

在仓库根目录执行脚本。脚本会同步维护 `docker-compose.yml` 和 `service/pom.xml`（因此无需手工改文件，也能直接用 Compose 编排部署）。

#### 新建业务服务（后端）

```powershell
.\tools\new-service.ps1 -ServiceName order-service -ArtifactId skylark-order-service -Port 18081
```

- 生成目录：`service/order-service/`
- 同步更新：`service/pom.xml` 的 `<modules>` + `docker-compose.yml`（新增 `order-service:` 段落）

#### 新建业务前端（web）

```powershell
.\tools\new-frontend.ps1 -AppName order-web -Port 9531 -Title "Order Web"
```

- 生成目录：`web/apps/order-web/`
- 同步更新：`docker-compose.yml`（新增 `order-web:` 段落）

#### 新增业务模块前先注册「应用」

在开发新的业务模块（尤其是新增一个前端应用 `web/apps/<app>`）之前，建议先在权限系统里 **新增一条应用记录**：

- **`oauth_client_details`**：新增 OAuth 客户端（`client_id` 推荐与前端 `VUE_APP_CLIENT_ID` 保持一致）
  - `web_server_redirect_uri` 需要包含 `http(s)://<域名或IP>:<端口>/home`（可逗号分隔多个）
- **`sys_oauth_client_meta`**：新增 `client_id`、`name`、`port`
  - `permission-app` 的 `/apps` 页面会使用 **当前域名/IP + meta 端口** 拼接跳转地址进入对应应用

#### 一次生成前后端

```powershell
.\tools\new-fullstack.ps1 -ServiceName order-service -WebAppName order-web -BackendPort 18081 -FrontendPort 9531 -Title "Order Web"
```

不传 `-WebAppName` 时，默认前端目录名为 `<ServiceName>-web`（例如 `order-service-web`）。

#### 移除后端服务（清理）

```powershell
.\tools\remove-service.ps1 -ServiceName order-service
```

会删除 `service/order-service/`，并从 `service/pom.xml` 移除 `<module>`，同时从 `docker-compose.yml` 删除对应 service 段落。

#### 移除前端应用（清理）

```powershell
.\tools\remove-web.ps1 -AppName order-web
```

会删除 `web/apps/order-web/`，并从 `docker-compose.yml` 删除对应 service 段落。

#### 一次移除前后端

```powershell
.\tools\remove-fullstack.ps1 -ServiceName order-service -WebAppName order-web
```

如需保留 `docker-compose.yml` 不变，可使用 `-KeepCompose`。

### 9. 常见问题（FAQ）

#### 数据库我怎么远程连接？

Compose 将容器 MySQL 的 `3306` 映射到宿主机 `3307`，因此从其他机器连接时：

- **Host**：Docker 所在宿主机的 IP/域名
- **Port**：`3307`
- **User/Password**：取决于 `MYSQL_ROOT_PASSWORD` 或 `MYSQL_USER/MYSQL_PASSWORD`

示例（命令行）：

```bash
mysql -h <host> -P 3307 -u root -p
```

#### `.env` 改库名为什么还是老的库？

请确认你改的是 `MYSQL_DATABASE`（而不是 `DB_URL` 之类自定义变量），并注意 `mysql_data` 数据卷存在时不会重新初始化；需要 `docker compose down -v` 才会让初始化库名重新生效。

#### 前端打包宿主机必须有 Node.js 环境吗？

不必须。如果使用 `docker compose build permission-app`，前端构建在镜像内完成；仅当你在宿主机直接运行 `pnpm run build/serve` 才需要 Node.js。


