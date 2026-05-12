## IoT（物联网）使用手册

[English](README.md) | 简体中文

本手册说明如何在当前仓库中**运行与使用 Skylark 物联网能力**：**`aiot-service`**（设备接入与管理 API）、**`aiot-web`**（控制台）、**EMQX**（MQTT Broker 与规则转发）以及 **MySQL**（元数据与运行记录）。与 `docs/permission` 下的权限手册并列，面向开发与联调人员。

### 范围说明

- **协议**：以 **`MQTT_ALINK_JSON`**（`/sys/{productKey}/{deviceKey}/...` 类 Alink Topic）为主。
- **部署**：以仓库根目录 **`docker-compose.yml`** 为主。
- **不包含**：阿里云公有云控制台物模型 TSL 的 `specs` 导入导出；物模型在本系统中的 JSON 形态见 [04. 物模型 JSON（Skylark 约定）](04-thing-model.zh-CN.md)。

### 章节目录

- [01. 系统架构](01-architecture.zh-CN.md)
- [02. 本地快速启动](02-quick-start.zh-CN.md)
- [03. 产品、设备与 MQTT 模拟联调](03-product-device-and-simulation.zh-CN.md)
- [04. 物模型 JSON（Skylark 约定）](04-thing-model.zh-CN.md)

### 仓库内相关路径

- 物联网服务：`service/aiot-service/`
- 物联网控制台：`web/apps/aiot-web/`
- EMQX 初始化脚本：`service/aiot-service/scripts/init-emqx.sh`
- 物模型编写细则：`web/apps/aiot-web/src/views/thing-model/THING_MODEL_AUTHORING.md`
