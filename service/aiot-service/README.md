# aiot-service

Skylark IoT 业务服务：设备与产品管理、上行接入（EMQX）、出站集成、告警与通知等能力；依赖 MySQL（Flyway 迁移）、可选接入权限服务做 RBAC。

## 本地运行

需要先有可用的 MySQL，并与 `application.yml` 中的库名一致（默认 `skylark_aiot`）。可用环境变量覆盖连接信息，例如：

- `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT`（默认配置为 `80`，本机开发常设为 `8080` 等）

在 `service/` 目录下构建 reactor 并启动本模块：

```bash
mvn -q install
mvn -q -pl aiot-service spring-boot:run
```

或在仓库根目录：

```bash
mvn -q -f service/pom.xml install
mvn -q -f service/pom.xml -pl aiot-service spring-boot:run
```

## HTTP 路径约定

业务接口统一挂在 `/api/aiot-service/...` 下，主要包括：

- **管理**：`/api/aiot-service/mgmt/...`（产品、物模型、设备、设备组等）
- **接入**：`/api/aiot-service/access/...`（设备上行、EMQX 相关）
- **应用集成（出站）**：`/api/aiot-service/appint/...`
- **告警**：`/api/aiot-service/alarm/...`
- **通知**：`/api/aiot-service/notification/...`

演示用探活（保留自模板）：

- `GET /api/business-service/demo/ping`

Actuator：`/actuator/health`、`/actuator/info`（参见 `management.endpoints` 配置）。

## 权限与数据域

编辑 `src/main/resources/application.yml` 中的 `skylark.authz`、`skylark.datadomain`。本服务默认 `app-code: aiot-service`；关闭权限校验可将 `skylark.authz.enabled` 设为 `false`（开发环境）。

租户与审计相关表名在 `skylark.datadomain.tenant-tables` 中维护（含 IoT 与通知等表）。

## 前端

对应控制台：`web/apps/aiot-web`。
