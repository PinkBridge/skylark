## 01. 系统架构

[English](01-architecture.md)

### 总体架构

本仓库物联网相关运行时主要包括三类组件：

- **`aiot-service`**：Spring Boot 服务，同时承担**设备接入**（EMQX 回调、上行解析）与**管理面**（产品、设备、物模型、告警、通知、出站集成等）。
- **EMQX**：MQTT Broker；通过 HTTP 回调做认证/鉴权；通过规则把设备上报转发到上行 Webhook。
- **MySQL**：存储产品、设备、ACL、物模型、属性/事件/服务运行记录等（IoT 库名默认 **`skylark_aiot`**，以 `application.yml` / Compose 为准）。

典型 Compose 部署下，`aiot-service` 在容器网络内通过服务名访问 **`emqx`**、**`mysql`**。

### 模块职责

#### 1. 认证与 ACL

- EMQX 调用 **`POST /api/aiot-service/access/emqx/auth`** 做用户名密码认证。
- EMQX 调用 **`POST /api/aiot-service/access/emqx/acl`** 做发布/订阅鉴权。
- 当启用 DB 策略时（`IOT_ACCESS_AUTH_USE_DB`、`IOT_ACCESS_ACL_USE_DB` 等），由 `aiot-service` 结合 **`iot_acl_policy`** 等表判定是否允许 `publish` / `subscribe`。

#### 2. 上行接入

- EMQX **规则**将匹配 Topic 的消息转发到 **`POST /api/aiot-service/access/upstream`**（具体规则见 `service/aiot-service/scripts/init-emqx.sh`）。
- `aiot-service` 解析 Alink 风格 Topic 与载荷，写入**属性 / 事件 / 服务回复**等运行表，并向内部发布**规范化事件**供出站、告警等模块消费。

#### 3. 会话 Webhook

- EMQX 将客户端上下线事件回调到 **`POST /api/aiot-service/access/emqx/webhook/session`**（可按配置携带 `X-Emqx-Webhook-Secret`）。

#### 4. 管理 API

- **`/api/aiot-service/mgmt/...`** 提供控制台使用的管理能力；在开启 Skylark 权限时走统一鉴权（通过网关或直连时注意携带合法身份）。

### 核心数据对象（概念）

- **产品 / 设备**主数据。
- **物模型**：产品维度 JSON（及可选设备快照）。
- **运行表**：属性上报、事件上报、服务调用与回复、连接日志等。
- **ACL 策略**：产品模板 + 设备实例策略。

### 关键链路

#### A. 属性上报

1. 设备发布到 `/sys/{productKey}/{deviceKey}/thing/event/property/post`（载荷含 `params`）。
2. EMQX 规则匹配 `/sys/+/+/thing/event/+/post` 并转发到上行 Webhook。
3. `aiot-service` 解析后写入**属性记录**，并产生对外的规范化事件。

#### B. 自定义事件

1. 设备发布到 `/sys/{productKey}/{deviceKey}/thing/event/{事件标识}/post`。
2. 同样经规则进入上行处理，写入**事件记录**。

#### C. 服务回复

1. 设备发布到 `/sys/{productKey}/{deviceKey}/thing/service/{服务标识}/reply`。
2. 经 `.../thing/service/+/reply` 规则进入上行，写入**服务相关记录**（实现以代码为准）。

### 设计要点

- **解耦**：连接与路由在 EMQX，业务在 `aiot-service`。
- **可追踪**：载荷中的 `id` / `messageId` 等用于关联（见上行实现）。
- **可重复初始化**：`emqx-init` 容器执行 `init-emqx.sh`，尽量幂等。

### 端口约定（Compose 默认映射）

| 组件           | 本机端口（默认） | 说明                 |
|----------------|------------------|----------------------|
| `aiot-service` | `9533` → 80      | 直连 IoT HTTP API   |
| `emqx`         | `1883`、`18083` | MQTT / Dashboard     |
| `mysql`        | `3307` → 3306    | 各业务库             |
| `aiot-web`     | `9532` → 80      | IoT 控制台           |
| `gateway`      | `80`（可配置）   | 可选统一入口         |

若你修改了 `.env` 中的端口映射，请以实际为准。
