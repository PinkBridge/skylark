## 02. 本地快速启动

[English](02-quick-start.md)

### 1. 前置条件

- 已安装 **Docker** 并支持 **Compose v2**。
- 在**仓库根目录**（存在 `docker-compose.yml`）操作。
- 预留本机端口（本文默认）：**`3307`**、**`1883`**、**`18083`**、**`9533`**；若启动网关/控制台还需 **`80`**、**`9532`** 等。

### 2. 环境变量

Compose 已为 `aiot-service` 配置 MySQL、EMQX 等。常用项见根目录 **`docker-compose.yml`** 与 **`service/aiot-service/src/main/resources/application.yml`**：

- **MySQL**：`SPRING_DATASOURCE_*`（默认示例常为 root / `123456`，库名 **`skylark_aiot`**）。
- **EMQX 控制台账号**：`EMQX_DASHBOARD_USERNAME` / `EMQX_DASHBOARD_PASSWORD`（默认 `admin` / `public`）。
- **Webhook 密钥**（建议配置）：`IOT_ACCESS_WEBHOOK_SECRET`，需与 **`emqx-init`** 使用的值一致，便于校验会话 Webhook。
- **EMQX 管理 API**（可选）：`EMQX_API_KEY` / `EMQX_API_SECRET`，供 `aiot-service` 侧访问 Broker 管理接口时使用。

仅启动 IoT 链路时，仍需保证 **MySQL 先就绪**（Compose 中 `depends_on` 已做健康依赖）。

### 3. 启动核心服务

在**仓库根目录**执行：

```bash
docker compose up -d mysql emqx aiot-service emqx-init
```

说明：

- **`emqx-init`** 会执行 `init-emqx.sh`，配置 HTTP 认证/ACL、桥接与转发规则。
- 若容器很快退出，查看日志：`docker logs emqx-init`。

若需要使用 **`aiot-web` 控制台**（产品/设备/物模型等），通常还需 **`permission`**、**`gateway`** 与 **`aiot-web`**（OAuth 与菜单）。示例：

```bash
docker compose up -d mysql permission gateway aiot-web emqx aiot-service emqx-init
```

### 4. 健康检查

- **`aiot-service`**：`http://127.0.0.1:9533/actuator/health`（若你改了主机端口映射，请替换 `9533`）。
- **EMQX 控制台**：`http://127.0.0.1:18083`，使用环境变量中的 Dashboard 账号登录。

### 5. 代码或脚本变更后

对受影响镜像执行重建：

```bash
docker compose up -d --build aiot-service emqx-init
```

若 `aiot-service` 的 Flyway 有新增迁移，重启服务后会自动执行。

### 6. 常见问题

- **`emqx-init` 报 `set: illegal option`**：确认 `init-emqx.sh` 为 **LF** 换行（仓库根 `.gitattributes` 已对 `*.sh` 约定）。
- **规则缺失**：清空 EMQX 数据卷后需重新跑 **`emqx-init`**，或按脚本说明手动调用 EMQX HTTP API。
- **`aiot-service` 连不上库**：确认 `mysql` 健康检查已为 **healthy** 再启动 IoT 服务。

### 7. 下一步

- 在 **`aiot-web`**（Compose 默认映射 **`9532`**）中维护产品与设备。
- 按 [03. 产品、设备与 MQTT 模拟联调](03-product-device-and-simulation.zh-CN.md) 做完整 MQTT 联调。
